package ru.cororo.corasense.repo.action

import ru.cororo.corasense.model.action.data.AdAction
import ru.cororo.corasense.model.action.data.AdActionStats
import ru.cororo.corasense.model.action.data.AdActionStatsDaily
import ru.cororo.corasense.repo.CachedCrudRepo
import java.util.*
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference
import kotlin.time.Duration.Companion.hours

class CachedAdActionRepo(backedRepo: AdActionRepo) : CachedCrudRepo<UUID, AdAction>(
    backedRepo, expiration = 1.hours
), AdActionRepo {
    private val userClicks = newCache<Pair<UUID, UUID>, AdAction?> {
        backedRepo.getUserClick(it.first, it.second)
    }
    private val userImpressions = newCache<Pair<UUID, UUID>, AdAction?> {
        backedRepo.getUserImpression(it.first, it.second)
    }
    private val campaignActionsCache = newCache<UUID, MutableMap<Pair<UUID, AdAction.Type>, AdAction>?> {
        backedRepo.getCampaignActions(it)?.also {
            it.values.forEach {
                cacheById[it.id] = it
            }
        }
    }
    private val advertiserActionsCache = newCache<UUID, MutableMap<Triple<UUID, UUID, AdAction.Type>, AdAction>?> {
        backedRepo.getAdvertiserActions(it)?.also {
            it.values.forEach {
                cacheById[it.id] = it
            }
        }
    }

    override suspend fun save(entity: AdAction) {
        super.save(entity)
        val campaignActions = getCampaignActions(entity.campaignId)
        if (campaignActions != null) {
            campaignActions[entity.clientId to entity.type] = entity
        }

        val advertiserActions = getAdvertiserActions(entity.advertiserId)
        if (advertiserActions != null) {
            advertiserActions[Triple(entity.clientId, entity.campaignId, entity.type)] = entity
        }

        if (entity.type == AdAction.Type.CLICK) {
            userClicks.put(entity.campaignId to entity.clientId, entity)
        } else {
            userImpressions.put(entity.campaignId to entity.clientId, entity)
        }
    }

    private fun getTotalStats(actions: Collection<AdAction>): AdActionStats {
        var impressionsCount = 0L
        var clicksCount = 0L
        var spentImpressions = 0.0
        var spentClicks = 0.0
        actions.forEach { action ->
            when (action.type) {
                AdAction.Type.IMPRESSION -> {
                    impressionsCount++
                    spentImpressions += action.cost
                }

                AdAction.Type.CLICK -> {
                    clicksCount++
                    spentClicks += action.cost
                }
            }
        }

        val conversion = if (impressionsCount > 0) {
            (clicksCount.toDouble() / impressionsCount.toDouble()) * 100
        } else 0.0
        return AdActionStats(
            impressionsCount, clicksCount, conversion, spentImpressions, spentClicks, spentClicks + spentImpressions
        )
    }

    private fun getDailyStats(actions: Collection<AdAction>): Map<Int, AdActionStatsDaily> {
        var impressionsCount = mutableMapOf<Int, AtomicLong>()
        var clicksCount = mutableMapOf<Int, AtomicLong>()
        var spentImpressions = mutableMapOf<Int, AtomicReference<Double>>()
        var spentClicks = mutableMapOf<Int, AtomicReference<Double>>()
        actions.forEach { action ->
            when (action.type) {
                AdAction.Type.IMPRESSION -> {
                    impressionsCount.getOrPut(action.day) { AtomicLong(0) }.incrementAndGet()
                    spentImpressions.getOrPut(action.day) { AtomicReference(0.0) }.let {
                        it.set(it.get() + action.cost)
                    }
                }

                AdAction.Type.CLICK -> {
                    clicksCount.getOrPut(action.day) { AtomicLong(0) }.incrementAndGet()
                    spentClicks.getOrPut(action.day) { AtomicReference(0.0) }.let {
                        it.set(it.get() + action.cost)
                    }
                }
            }
        }

        return impressionsCount.mapValues { (day, impressionsCount) ->
            val clicksCount = clicksCount[day]?.get() ?: 0
            val spentImpressions = spentImpressions[day]?.get() ?: 0.0
            val spentClicks = spentClicks[day]?.get() ?: 0.0
            val conversion = if (impressionsCount.get() > 0) {
                (clicksCount.toDouble() / impressionsCount.get().toDouble()) * 100
            } else 0.0

            AdActionStatsDaily(
                impressionsCount.get(),
                clicksCount,
                conversion,
                spentImpressions,
                spentClicks,
                spentClicks + spentImpressions,
                day
            )
        }
    }

    override suspend fun getTotalCampaignStats(campaignId: UUID): AdActionStats {
        val actions = getCampaignActions(campaignId) ?: return AdActionStats.EMPTY
        return getTotalStats(actions.values)
    }

    override suspend fun getDailyCampaignStats(campaignId: UUID): Map<Int, AdActionStatsDaily> {
        val actions = getCampaignActions(campaignId) ?: return mapOf()
        return getDailyStats(actions.values)
    }

    override suspend fun getTotalAdvertiserStats(advertiserId: UUID): AdActionStats {
        val actions = getAdvertiserActions(advertiserId) ?: return AdActionStats.EMPTY
        return getTotalStats(actions.values)
    }

    override suspend fun getDailyAdvertiserStats(advertiserId: UUID): Map<Int, AdActionStatsDaily> {
        val actions = getAdvertiserActions(advertiserId) ?: return mapOf()
        return getDailyStats(actions.values)
    }

    override suspend fun getUserClick(campaignId: UUID, clientId: UUID): AdAction? =
        userClicks.get(campaignId to clientId)

    override suspend fun reachedClickLimit(campaignId: UUID): Boolean =
        (backedRepo as AdActionRepo).reachedClickLimit(campaignId)

    override suspend fun getUserImpression(campaignId: UUID, clientId: UUID): AdAction? =
        userImpressions.get(campaignId to clientId)

    override suspend fun deleteAllByCampaignId(campaignId: UUID) {
        (backedRepo as AdActionRepo).deleteAllByCampaignId(campaignId)
        var advertiserActions: MutableMap<Triple<UUID, UUID, AdAction.Type>, AdAction>? = null
        campaignActionsCache.get(campaignId)?.forEach { (_, value) ->
            userClicks.invalidate(campaignId to value.clientId)
            userImpressions.invalidate(campaignId to value.clientId)
            cacheById.invalidate(value.id)
            if (advertiserActions == null) {
                advertiserActions = getAdvertiserActions(value.advertiserId)
            }

            advertiserActions?.remove(Triple(value.clientId, value.campaignId, value.type))
        }

        campaignActionsCache.invalidate(campaignId)
    }

    override suspend fun getCampaignActions(campaignId: UUID) = campaignActionsCache.get(campaignId)

    override suspend fun getAdvertiserActions(advertiserId: UUID) = advertiserActionsCache.get(advertiserId)
}
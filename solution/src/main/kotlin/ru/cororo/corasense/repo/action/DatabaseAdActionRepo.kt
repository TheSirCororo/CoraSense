package ru.cororo.corasense.repo.action

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import ru.cororo.corasense.model.action.data.AdAction
import ru.cororo.corasense.model.action.data.AdActionStats
import ru.cororo.corasense.model.action.data.AdActionStatsDaily
import ru.cororo.corasense.plugin.sql
import ru.cororo.corasense.repo.campaign.CampaignTable
import ru.cororo.corasense.util.deleteById
import ru.cororo.corasense.util.getById
import java.util.*

object DatabaseAdActionRepo : AdActionRepo {
    override suspend fun AdAction.getId(): UUID = id

    override suspend fun save(entity: AdAction): Unit = sql {
        AdActionTable.upsert(AdActionTable.campaignId, AdActionTable.clientId, AdActionTable.type, onUpdate = {
            with(AdActionTable) {
                it[campaignId] = entity.campaignId
                it[clientId] = entity.clientId
                it[type] = entity.type
            }
        }) {
            it[campaignId] = entity.campaignId
            it[advertiserId] = entity.advertiserId
            it[clientId] = entity.clientId
            it[type] = entity.type
            it[day] = entity.day
            it[cost] = entity.cost
        }
    }

    override suspend fun saveAll(entities: Iterable<AdAction>) = sql {
        entities.forEach {
            save(it)
        }
    }

    override suspend fun delete(id: UUID): Unit = sql {
        AdActionTable.deleteById(id)
    }

    override suspend fun get(id: UUID): AdAction? = sql {
        AdActionTable.getById(id)?.let { toAdAction(it) }
    }

    override suspend fun getTotalCampaignStats(campaignId: UUID): AdActionStats =
        error("Имплементировано только в CachedRepo")

    override suspend fun getDailyCampaignStats(campaignId: UUID): Map<Int, AdActionStatsDaily> =
        error("Имплементировано только в CachedRepo")

    override suspend fun getTotalAdvertiserStats(advertiserId: UUID): AdActionStats =
        error("Имплементировано только в CachedRepo")

    override suspend fun getDailyAdvertiserStats(advertiserId: UUID): Map<Int, AdActionStatsDaily> =
        error("Имплементировано только в CachedRepo")

    override suspend fun getUserImpression(campaignId: UUID, clientId: UUID): AdAction? = sql {
        AdActionTable.selectAll()
            .where { (AdActionTable.campaignId eq campaignId) and (AdActionTable.clientId eq clientId) and (AdActionTable.type eq AdAction.Type.IMPRESSION) }
            .singleOrNull()?.let { toAdAction(it) }
    }

    override suspend fun getUserClick(campaignId: UUID, clientId: UUID): AdAction? = sql {
        AdActionTable.selectAll()
            .where { (AdActionTable.campaignId eq campaignId) and (AdActionTable.clientId eq clientId) and (AdActionTable.type eq AdAction.Type.CLICK) }
            .singleOrNull()?.let { toAdAction(it) }
    }

    override suspend fun reachedClickLimit(campaignId: UUID): Boolean = sql {
        val count = AdActionTable.id.count().over().alias("click_count")
        val countQuery = AdActionTable.select(count, AdActionTable.campaignId)
            .where { (AdActionTable.campaignId eq campaignId) and (AdActionTable.type eq AdAction.Type.CLICK) }
            .groupBy(AdActionTable.campaignId, AdActionTable.id)
            .alias("count_query")
        val limitReached = Op.build { countQuery[count] greaterEq CampaignTable.clicksLimit.castTo(LongColumnType()) }
            .alias("limit_reached")
        CampaignTable.leftJoin(countQuery) { CampaignTable.id eq countQuery[AdActionTable.campaignId] }
            .select(limitReached as Expression<*>)
            .where { CampaignTable.id eq campaignId }
            .singleOrNull()
            ?.getOrNull(limitReached) == true
    }

    override suspend fun deleteAllByCampaignId(campaignId: UUID): Unit = sql {
        AdActionTable.deleteWhere { AdActionTable.campaignId eq campaignId }
    }

    override suspend fun getCampaignActions(campaignId: UUID): MutableMap<Pair<UUID, AdAction.Type>, AdAction>? = sql {
        AdActionTable.selectAll().where { AdActionTable.campaignId eq campaignId }.associateTo(mutableMapOf()) {
            val action = toAdAction(it)
            (action.clientId to action.type) to action
        }
    }

    override suspend fun getAdvertiserActions(advertiserId: UUID): MutableMap<Triple<UUID, UUID, AdAction.Type>, AdAction>? =
        sql {
            AdActionTable.selectAll().where { AdActionTable.advertiserId eq advertiserId }.associateTo(mutableMapOf()) {
                val action = toAdAction(it)
                Triple(action.clientId, action.campaignId, action.type) to action
            }
        }

    private fun toAdAction(result: ResultRow) =
        with(AdActionTable) {
            AdAction(
                result[id].value,
                result[advertiserId].value,
                result[campaignId].value,
                result[clientId].value,
                result[type],
                result[day],
                result[cost]
            )
        }
}
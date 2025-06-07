package ru.cororo.corasense.service

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import ru.cororo.corasense.shared.model.action.AdAction
import ru.cororo.corasense.shared.model.action.AdActionStats
import ru.cororo.corasense.shared.model.action.AdActionStatsDaily
import ru.cororo.corasense.repo.action.AdActionRepo
import ru.cororo.corasense.shared.service.AdActionService
import ru.cororo.corasense.shared.service.CurrentDayService
import java.util.*
import kotlin.coroutines.CoroutineContext

class AdActionServiceImpl(
    private val adActionRepo: AdActionRepo,
    private val currentDayService: CurrentDayService,
    private val micrometerService: MicrometerService
) : AdActionService, CoroutineScope {

    override val coroutineContext: CoroutineContext = Dispatchers.Default + CoroutineName("AdActionServiceImpl")

    override suspend fun getTotalCampaignStats(campaignId: UUID) =
        adActionRepo.getTotalCampaignStats(campaignId)

    override suspend fun getDailyCampaignStats(campaignId: UUID): List<AdActionStatsDaily> {
        val result = adActionRepo.getDailyCampaignStats(campaignId)

        return (0..currentDayService.getCurrentDay()).map {
            result[it] ?: AdActionStatsDaily(0, 0, 0.0, 0.0, 0.0, 0.0, it)
        }
    }

    override suspend fun getTotalAdvertiserStats(advertiserId: UUID): AdActionStats =
        adActionRepo.getTotalAdvertiserStats(advertiserId)

    override suspend fun getDailyAdvertiserStats(advertiserId: UUID): List<AdActionStatsDaily> {
        val result = adActionRepo.getDailyAdvertiserStats(advertiserId)
        return (0..currentDayService.getCurrentDay()).map {
            result[it] ?: AdActionStatsDaily(0, 0, 0.0, 0.0, 0.0, 0.0, it)
        }
    }

    override suspend fun saveAction(action: AdAction) {
        adActionRepo.save(action).also {
            micrometerService.markToUpdateCampaign(action.campaignId)
            micrometerService.markToUpdateAdvertiser(action.advertiserId)
        }
    }

    override suspend fun hasImpressed(clientId: UUID, campaignId: UUID) =
        adActionRepo.getUserImpression(campaignId, clientId) != null

    override suspend fun getUserClick(clientId: UUID, campaignId: UUID) = adActionRepo.getUserClick(campaignId, clientId)

    override suspend fun reachedClickLimit(campaignId: UUID) = adActionRepo.reachedClickLimit(campaignId)
}
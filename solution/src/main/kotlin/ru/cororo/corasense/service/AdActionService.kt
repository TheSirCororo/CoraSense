package ru.cororo.corasense.service

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ru.cororo.corasense.model.action.data.AdAction
import ru.cororo.corasense.model.action.data.AdActionStats
import ru.cororo.corasense.model.action.data.AdActionStatsDaily
import ru.cororo.corasense.repo.action.AdActionRepo
import java.util.*

class AdActionService(
    private val adActionRepo: AdActionRepo,
    private val currentDayService: CurrentDayService,
    private val micrometerService: MicrometerService
) {
    private val mutex = Mutex()

    suspend fun getTotalCampaignStats(campaignId: UUID) =
        adActionRepo.getTotalCampaignStats(campaignId)

    suspend fun getDailyCampaignStats(campaignId: UUID): List<AdActionStatsDaily> {
        val result = adActionRepo.getDailyCampaignStats(campaignId)

        return (0..currentDayService.getCurrentDay()).map {
            result[it] ?: AdActionStatsDaily(0, 0, 0.0, 0.0, 0.0, 0.0, it)
        }
    }

    suspend fun getTotalAdvertiserStats(advertiserId: UUID): AdActionStats =
        adActionRepo.getTotalAdvertiserStats(advertiserId)

    suspend fun getDailyAdvertiserStats(advertiserId: UUID): List<AdActionStatsDaily> {
        val result = adActionRepo.getDailyAdvertiserStats(advertiserId)
        return (0..currentDayService.getCurrentDay()).map {
            result[it] ?: AdActionStatsDaily(0, 0, 0.0, 0.0, 0.0, 0.0, it)
        }
    }

    suspend fun saveAction(action: AdAction) = mutex.withLock {
        adActionRepo.save(action).also {
            micrometerService.markToUpdateCampaign(action.campaignId)
            micrometerService.markToUpdateAdvertiser(action.advertiserId)
        }
    }

    suspend fun hasImpressed(clientId: UUID, campaignId: UUID) =
        adActionRepo.getUserImpression(campaignId, clientId) != null

    suspend fun getUserClick(clientId: UUID, campaignId: UUID) = adActionRepo.getUserClick(campaignId, clientId)

    suspend fun reachedClickLimit(campaignId: UUID) = adActionRepo.reachedClickLimit(campaignId)
}
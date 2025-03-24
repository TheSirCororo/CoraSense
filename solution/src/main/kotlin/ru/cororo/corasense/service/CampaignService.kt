package ru.cororo.corasense.service

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ru.cororo.corasense.model.campaign.data.Campaign
import ru.cororo.corasense.model.campaign.dto.CampaignCreateRequest
import ru.cororo.corasense.model.client.data.Client
import ru.cororo.corasense.repo.action.AdActionRepo
import ru.cororo.corasense.repo.campaign.CampaignRepo
import java.util.*

class CampaignService(
    private val campaignRepo: CampaignRepo,
    private val adActionRepo: AdActionRepo,
    private val currentDayService: CurrentDayService,
    private val micrometerService: MicrometerService
) {
    private val mutex = Mutex()

    suspend fun createCampaign(advertiserId: UUID, request: CampaignCreateRequest) =
        campaignRepo.createCampaign(advertiserId, request)

    suspend fun saveCampaign(campaign: Campaign) = campaignRepo.save(campaign).also {
        micrometerService.markToUpdateCampaign(campaign.id)
        micrometerService.markToUpdateAdvertiser(campaign.advertiserId)
    }

    suspend fun getCampaign(id: UUID) = campaignRepo.get(id)

    suspend fun getAdvertiserCampaigns(id: UUID, offset: Long, limit: Int): Pair<Set<Campaign>, Long> =
        if (offset >= 0) campaignRepo.getAdvertiserCampaigns(id, offset, limit) else (setOf<Campaign>() to 0L)

    suspend fun deleteCampaign(id: UUID) {
        val campaign = getCampaign(id) ?: return
        adActionRepo.deleteAllByCampaignId(id)
        campaignRepo.delete(id)
        micrometerService.deleteCampaignStats(id)
        micrometerService.markToUpdateAdvertiser(campaign.advertiserId)
    }

    suspend fun getRelevantCampaignForClient(client: Client) = mutex.withLock {
        campaignRepo.getRelevantCampaignForClient(client, currentDayService.getCurrentDay())
    }

    suspend fun getAllCampaigns() = campaignRepo.getAll()
}
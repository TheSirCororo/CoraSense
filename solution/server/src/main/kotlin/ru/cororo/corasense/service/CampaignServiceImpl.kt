package ru.cororo.corasense.service

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Dispatchers
import ru.cororo.corasense.repo.action.AdActionRepo
import ru.cororo.corasense.repo.campaign.CampaignRepo
import ru.cororo.corasense.shared.model.campaign.Campaign
import ru.cororo.corasense.shared.model.campaign.CampaignCreateData
import ru.cororo.corasense.shared.model.client.Client
import ru.cororo.corasense.shared.service.CampaignService
import ru.cororo.corasense.shared.service.CurrentDayService
import ru.cororo.corasense.shared.util.PagedEntities
import ru.cororo.corasense.shared.util.emptyPaged
import java.util.*
import kotlin.coroutines.CoroutineContext

class CampaignServiceImpl(
    private val campaignRepo: CampaignRepo,
    private val adActionRepo: AdActionRepo,
    private val currentDayService: CurrentDayService,
    private val micrometerService: MicrometerService
) : CampaignService {

    override val coroutineContext: CoroutineContext = Dispatchers.Default + CoroutineName("CampaignServiceImpl")

    override suspend fun createCampaign(advertiserId: UUID, data: CampaignCreateData) =
        campaignRepo.createCampaign(advertiserId, data)

    override suspend fun saveCampaign(campaign: Campaign) = campaignRepo.save(campaign).also {
        micrometerService.markToUpdateCampaign(campaign.id)
        micrometerService.markToUpdateAdvertiser(campaign.advertiserId)
    }

    override suspend fun getCampaign(id: UUID) = campaignRepo.get(id)

    override suspend fun getAdvertiserCampaigns(id: UUID, offset: Long, limit: Int): PagedEntities<Campaign> =
        if (offset >= 0) campaignRepo.getAdvertiserCampaigns(id, offset, limit) else emptyPaged()

    override suspend fun deleteCampaign(id: UUID) {
        val campaign = getCampaign(id) ?: return
        adActionRepo.deleteAllByCampaignId(id)
        campaignRepo.delete(id)
        micrometerService.deleteCampaignStats(id)
        micrometerService.markToUpdateAdvertiser(campaign.advertiserId)
    }

    override suspend fun getRelevantCampaignForClient(client: Client) =
        campaignRepo.getRelevantCampaignForClient(client, currentDayService.getCurrentDay())

    override suspend fun getAllCampaigns() = campaignRepo.getAll()
}
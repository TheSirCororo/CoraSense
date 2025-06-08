package ru.cororo.corasense.shared.service

import kotlinx.rpc.RemoteService
import kotlinx.rpc.annotations.Rpc
import ru.cororo.corasense.shared.model.campaign.Campaign
import ru.cororo.corasense.shared.model.campaign.CampaignCreateData
import ru.cororo.corasense.shared.model.client.Client
import ru.cororo.corasense.shared.util.PagedEntities
import ru.cororo.corasense.shared.util.UuidString

@Rpc
interface CampaignService : RemoteService {
    suspend fun createCampaign(advertiserId: UuidString, data: CampaignCreateData): Campaign

    suspend fun saveCampaign(campaign: Campaign)

    suspend fun getCampaign(id: UuidString): Campaign?

    suspend fun deleteCampaign(id: UuidString)

    suspend fun getAdvertiserCampaigns(id: UuidString, offset: Long, limit: Int): PagedEntities<Campaign>

    suspend fun getRelevantCampaignForClient(client: Client): Campaign?

    suspend fun getAllCampaigns(): Set<Campaign>
}
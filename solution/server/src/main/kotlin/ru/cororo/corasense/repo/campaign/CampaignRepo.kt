package ru.cororo.corasense.repo.campaign

import ru.cororo.corasense.shared.model.campaign.Campaign
import ru.cororo.corasense.shared.model.campaign.CampaignCreateData
import ru.cororo.corasense.shared.model.client.Client
import ru.cororo.corasense.repo.CrudRepo
import ru.cororo.corasense.shared.util.PagedEntities
import java.util.*

interface CampaignRepo : CrudRepo<UUID, Campaign> {
    suspend fun createCampaign(advertiserId: UUID, request: CampaignCreateData): Campaign

    suspend fun getAdvertiserCampaigns(advertiserId: UUID, offset: Long, limit: Int): PagedEntities<Campaign>

    suspend fun getRelevantCampaignForClient(client: Client, day: Int): Campaign?

    suspend fun getAll(): Set<Campaign>
}
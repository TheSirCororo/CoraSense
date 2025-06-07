package ru.cororo.corasense.repo.campaign

import ru.cororo.corasense.model.campaign.data.Campaign
import ru.cororo.corasense.model.campaign.dto.CampaignCreateRequest
import ru.cororo.corasense.model.client.data.Client
import ru.cororo.corasense.repo.CrudRepo
import java.util.*

interface CampaignRepo : CrudRepo<UUID, Campaign> {
    suspend fun createCampaign(advertiserId: UUID, request: CampaignCreateRequest): Campaign

    suspend fun getAdvertiserCampaigns(advertiserId: UUID, offset: Long, limit: Int): Pair<Set<Campaign>, Long>

    suspend fun getRelevantCampaignForClient(client: Client, day: Int): Campaign?

    suspend fun getAll(): List<Campaign>
}
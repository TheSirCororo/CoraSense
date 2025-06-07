package ru.cororo.corasense.repo.campaign

import org.koin.core.component.KoinComponent
import ru.cororo.corasense.shared.model.campaign.Campaign
import ru.cororo.corasense.shared.model.campaign.CampaignCreateData
import ru.cororo.corasense.shared.model.client.Client
import ru.cororo.corasense.repo.CachedCrudRepo
import ru.cororo.corasense.shared.util.PagedEntities
import ru.cororo.corasense.shared.util.emptyPaged
import ru.cororo.corasense.shared.util.paged
import java.util.*
import kotlin.math.min
import kotlin.time.Duration.Companion.seconds

class CachedCampaignRepo(backedRepo: CampaignRepo) :
    CachedCrudRepo<UUID, Campaign>(backedRepo, expiration = Int.MAX_VALUE.seconds), CampaignRepo, KoinComponent {
    private var allFetched = false

    override suspend fun createCampaign(advertiserId: UUID, request: CampaignCreateData): Campaign {
        val campaign = (backedRepo as CampaignRepo).createCampaign(advertiserId, request)
        cacheById.put(campaign.id, campaign)
        return campaign
    }

    override suspend fun getAdvertiserCampaigns(
        advertiserId: UUID,
        offset: Long,
        limit: Int
    ): PagedEntities<Campaign> = if (!allFetched) {
        (backedRepo as CampaignRepo).getAdvertiserCampaigns(advertiserId, offset, limit) // не кешируем
    } else {
        getAll().filter { it.advertiserId == advertiserId }.let {
            if (offset + 1 > it.size) emptyPaged()
            else it.subList(offset.toInt(), min(it.size, offset.toInt() + limit)).toSet().paged(it.size.toLong())
        }
    }

    override suspend fun getRelevantCampaignForClient(
        client: Client,
        day: Int
    ): Campaign? = (backedRepo as CampaignRepo).getRelevantCampaignForClient(client, day) // не кешируем

    override suspend fun getAll(): Set<Campaign> {
        if (allFetched) return cacheById.asMap().values.filterNotNullTo(mutableSetOf())

        val values = (backedRepo as CampaignRepo).getAll()
        for (campaign in values) {
            cacheById.put(campaign.id, campaign)
        }

        allFetched = true
        return values
    }
}

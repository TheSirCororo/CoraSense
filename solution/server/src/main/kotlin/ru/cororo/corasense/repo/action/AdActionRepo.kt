package ru.cororo.corasense.repo.action

import ru.cororo.corasense.model.action.data.AdAction
import ru.cororo.corasense.model.action.data.AdActionStats
import ru.cororo.corasense.model.action.data.AdActionStatsDaily
import ru.cororo.corasense.repo.CrudRepo
import java.util.UUID

interface AdActionRepo : CrudRepo<UUID, AdAction> {
    suspend fun getTotalCampaignStats(campaignId: UUID): AdActionStats

    suspend fun getDailyCampaignStats(campaignId: UUID): Map<Int, AdActionStatsDaily>

    suspend fun getTotalAdvertiserStats(advertiserId: UUID): AdActionStats

    suspend fun getDailyAdvertiserStats(advertiserId: UUID): Map<Int, AdActionStatsDaily>

    suspend fun getUserImpression(campaignId: UUID, clientId: UUID): AdAction?

    suspend fun getUserClick(campaignId: UUID, clientId: UUID): AdAction?

    suspend fun reachedClickLimit(campaignId: UUID): Boolean

    suspend fun deleteAllByCampaignId(campaignId: UUID)

    suspend fun getCampaignActions(campaignId: UUID): MutableMap<Pair<UUID, AdAction.Type>, AdAction>?

    suspend fun getAdvertiserActions(advertiserId: UUID): MutableMap<Triple<UUID, UUID, AdAction.Type>, AdAction>?
}

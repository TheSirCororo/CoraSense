package ru.cororo.corasense.shared.service

import kotlinx.rpc.RemoteService
import kotlinx.rpc.annotations.Rpc
import ru.cororo.corasense.shared.model.action.AdAction
import ru.cororo.corasense.shared.model.action.AdActionStats
import ru.cororo.corasense.shared.model.action.AdActionStatsDaily
import ru.cororo.corasense.shared.util.UuidString

@Rpc
interface AdActionService : RemoteService {
    suspend fun getTotalCampaignStats(campaignId: UuidString): AdActionStats

    suspend fun getDailyCampaignStats(campaignId: UuidString): List<AdActionStatsDaily>

    suspend fun getTotalAdvertiserStats(advertiserId: UuidString): AdActionStats

    suspend fun getDailyAdvertiserStats(advertiserId: UuidString): List<AdActionStatsDaily>

    suspend fun saveAction(action: AdAction)

    suspend fun hasImpressed(clientId: UuidString, campaignId: UuidString): Boolean

    suspend fun getUserClick(clientId: UuidString, campaignId: UuidString): AdAction?

    suspend fun reachedClickLimit(campaignId: UuidString): Boolean
}
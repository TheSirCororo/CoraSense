package ru.cororo.corasense.shared.service

import kotlinx.rpc.RemoteService
import kotlinx.rpc.annotations.Rpc
import ru.cororo.corasense.shared.model.moderation.ModerationVerdict

@Rpc
interface LlmService : RemoteService {
    suspend fun moderateText(text: String): ModerationVerdict

    suspend fun generateCampaignText(campaignTitle: String, advertiserName: String): String

    suspend fun isActive(): Boolean
}
package ru.cororo.corasense.model.llm.dto

import kotlinx.serialization.Serializable

@Serializable
data class GenerateCampaignTextResponse(
    val text: String
)
package ru.cororo.corasense.model.action.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AdActionStats(
    @SerialName("impressions_count")
    val impressionsCount: Long,
    @SerialName("clicks_count")
    val clicksCount: Long,
    val conversion: Double,
    @SerialName("spent_impressions")
    val spentImpressions: Double,
    @SerialName("spent_clicks")
    val spentClicks: Double,
    @SerialName("spent_total")
    val spentTotal: Double
) {
    companion object {
        val EMPTY = AdActionStats(0, 0, 0.0, 0.0, 0.0, 0.0)
    }
}
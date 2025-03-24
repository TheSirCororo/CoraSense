package ru.cororo.corasense.repo.ml

import org.jetbrains.exposed.sql.Table
import ru.cororo.corasense.repo.advertiser.AdvertiserTable
import ru.cororo.corasense.repo.client.ClientTable

object MLScoreTable : Table("ml_scores") {
    val advertiserId = reference("advertiser_id", AdvertiserTable)
    val clientId = reference("client_id", ClientTable)
    val score = integer("score")

    override val primaryKey: PrimaryKey = PrimaryKey(advertiserId, clientId)
}
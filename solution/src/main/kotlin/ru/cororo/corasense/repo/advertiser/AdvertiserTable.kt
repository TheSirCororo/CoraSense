package ru.cororo.corasense.repo.advertiser

import org.jetbrains.exposed.dao.id.UUIDTable

object AdvertiserTable : UUIDTable("advertisers") {
    val name = varchar("name", 128)
}
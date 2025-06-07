package ru.cororo.corasense.repo.advertiser

import ru.cororo.corasense.model.advertiser.data.Advertiser
import ru.cororo.corasense.repo.CrudRepo
import java.util.UUID

interface AdvertiserRepo : CrudRepo<UUID, Advertiser> {
    suspend fun getAll(): List<Advertiser>

    suspend fun findByName(name: String): List<Advertiser>
}
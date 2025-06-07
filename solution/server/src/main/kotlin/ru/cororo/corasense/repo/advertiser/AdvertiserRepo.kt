package ru.cororo.corasense.repo.advertiser

import ru.cororo.corasense.shared.model.advertiser.Advertiser
import ru.cororo.corasense.repo.CrudRepo
import java.util.UUID

interface AdvertiserRepo : CrudRepo<UUID, Advertiser> {
    suspend fun getAll(): Set<Advertiser>

    suspend fun findByName(name: String): Set<Advertiser>
}
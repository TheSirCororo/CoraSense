package ru.cororo.corasense.repo.advertiser

import ru.cororo.corasense.model.advertiser.data.Advertiser
import ru.cororo.corasense.repo.CachedCrudRepo
import java.util.UUID

class CachedAdvertiserRepo(backedRepo: AdvertiserRepo) : CachedCrudRepo<UUID, Advertiser>(backedRepo), AdvertiserRepo {
    override suspend fun getAll(): List<Advertiser> {
        val values = (backedRepo as AdvertiserRepo).getAll()
        for (advertiser in values) {
            cacheById.put(advertiser.id, advertiser)
        }

        return values
    }

    override suspend fun findByName(name: String): List<Advertiser> = (backedRepo as AdvertiserRepo).findByName(name)
}
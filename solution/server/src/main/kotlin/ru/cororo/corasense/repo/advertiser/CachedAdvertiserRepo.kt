package ru.cororo.corasense.repo.advertiser

import ru.cororo.corasense.shared.model.advertiser.Advertiser
import ru.cororo.corasense.repo.CachedCrudRepo
import java.util.UUID

class CachedAdvertiserRepo(backedRepo: AdvertiserRepo) : CachedCrudRepo<UUID, Advertiser>(backedRepo), AdvertiserRepo {
    override suspend fun getAll(): Set<Advertiser> {
        val values = (backedRepo as AdvertiserRepo).getAll()
        for (advertiser in values) {
            cacheById.put(advertiser.id, advertiser)
        }

        return values
    }

    override suspend fun findByName(name: String): Set<Advertiser> = (backedRepo as AdvertiserRepo).findByName(name)
}
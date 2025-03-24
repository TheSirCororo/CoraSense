package ru.cororo.corasense.service

import ru.cororo.corasense.model.advertiser.data.Advertiser
import ru.cororo.corasense.repo.advertiser.AdvertiserRepo
import java.util.UUID

class AdvertiserService(private val advertiserRepo: AdvertiserRepo, private val micrometerService: MicrometerService) {
    suspend fun getAdvertiser(id: UUID) = advertiserRepo.get(id)

    suspend fun saveAdvertiser(advertiser: Advertiser) = advertiserRepo.save(advertiser).also {
        micrometerService.markToUpdateAdvertiser(advertiser.id)
    }

    suspend fun saveAdvertisers(advertisers: Iterable<Advertiser>) = advertiserRepo.saveAll(advertisers).also {
        for (advertiser in advertisers) {
            micrometerService.markToUpdateAdvertiser(advertiser.id)
        }
    }

    suspend fun findAdvertisersByName(name: String): List<Advertiser> = advertiserRepo.findByName(name)

    suspend fun getAllAdvertisers() = advertiserRepo.getAll()
}
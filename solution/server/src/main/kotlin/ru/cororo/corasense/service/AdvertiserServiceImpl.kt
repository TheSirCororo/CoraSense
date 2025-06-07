package ru.cororo.corasense.service

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import ru.cororo.corasense.shared.model.advertiser.Advertiser
import ru.cororo.corasense.repo.advertiser.AdvertiserRepo
import ru.cororo.corasense.shared.service.AdvertiserService
import java.util.UUID
import kotlin.coroutines.CoroutineContext

class AdvertiserServiceImpl(private val advertiserRepo: AdvertiserRepo, private val micrometerService: MicrometerService) : AdvertiserService , CoroutineScope {

    override val coroutineContext: CoroutineContext = Dispatchers.Default + CoroutineName("AdvertiserServiceImpl")

    override suspend fun getAdvertiser(id: UUID) = advertiserRepo.get(id)

    override suspend fun saveAdvertiser(advertiser: Advertiser) = advertiserRepo.save(advertiser).also {
        micrometerService.markToUpdateAdvertiser(advertiser.id)
    }

    override suspend fun saveAdvertisers(advertisers: Iterable<Advertiser>) = advertiserRepo.saveAll(advertisers).also {
        for (advertiser in advertisers) {
            micrometerService.markToUpdateAdvertiser(advertiser.id)
        }
    }

    override suspend fun findAdvertisersByName(name: String): Set<Advertiser> = advertiserRepo.findByName(name)

    override suspend fun getAllAdvertisers() = advertiserRepo.getAll()
}
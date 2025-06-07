package ru.cororo.corasense.shared.service

import kotlinx.rpc.annotations.Rpc
import ru.cororo.corasense.shared.model.advertiser.Advertiser
import ru.cororo.corasense.shared.util.UuidString

@Rpc
interface AdvertiserService {
    suspend fun getAdvertiser(id: UuidString): Advertiser?

    suspend fun saveAdvertiser(advertiser: Advertiser)

    suspend fun saveAdvertisers(advertisers: Iterable<Advertiser>)

    suspend fun findAdvertisersByName(name: String): Set<Advertiser>

    suspend fun getAllAdvertisers(): Set<Advertiser>
}
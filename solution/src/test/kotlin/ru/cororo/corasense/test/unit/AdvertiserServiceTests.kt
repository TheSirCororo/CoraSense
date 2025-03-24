package ru.cororo.corasense.test.unit

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.Runs
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import ru.cororo.corasense.model.advertiser.data.Advertiser
import ru.cororo.corasense.repo.advertiser.AdvertiserRepo
import ru.cororo.corasense.service.AdvertiserService
import ru.cororo.corasense.service.MicrometerService
import java.util.UUID

// unit тесты написаны при помощи чата гпт
class AdvertiserServiceTests : StringSpec({
    val advertiserRepo = mockk<AdvertiserRepo>()
    val micrometerService = mockk<MicrometerService>()
    val advertiserService = AdvertiserService(advertiserRepo, micrometerService)

    beforeTest {
        clearAllMocks()
    }

    "should return expected advertiser by ID" {
        val id = UUID.randomUUID()
        val advertiser = Advertiser(id, "Test Advertiser")
        coEvery { advertiserRepo.get(id) } returns advertiser

        val result = advertiserService.getAdvertiser(id)
        result shouldBe advertiser

        coVerify(exactly = 1) { advertiserRepo.get(id) }
    }

    "should save advertiser and call micrometer" {
        val id = UUID.randomUUID()
        val advertiser = Advertiser(id, "Test Advertiser")

        coEvery { advertiserRepo.save(advertiser) } just Runs
        coEvery { micrometerService.markToUpdateAdvertiser(id) } just Runs

        advertiserService.saveAdvertiser(advertiser)

        coVerify(exactly = 1) { advertiserRepo.save(advertiser) }
        coVerify(exactly = 1) { micrometerService.markToUpdateAdvertiser(id) }
    }

    "should save multiple advertisers and call micrometer for each" {
        val advertiser1 = Advertiser(UUID.randomUUID(), "Advertiser 1")
        val advertiser2 = Advertiser(UUID.randomUUID(), "Advertiser 2")
        val advertisers = listOf(advertiser1, advertiser2)

        coEvery { advertiserRepo.saveAll(advertisers) } just Runs
        coEvery { micrometerService.markToUpdateAdvertiser(any()) } just Runs

        advertiserService.saveAdvertisers(advertisers)

        coVerify(exactly = 1) { advertiserRepo.saveAll(advertisers) }
        coVerify(exactly = 1) { micrometerService.markToUpdateAdvertiser(advertiser1.id) }
        coVerify(exactly = 1) { micrometerService.markToUpdateAdvertiser(advertiser2.id) }
    }

    "should return advertisers by name" {
        val advertisers = listOf(Advertiser(UUID.randomUUID(), "Test Advertiser"))
        coEvery { advertiserRepo.findByName("Test") } returns advertisers

        val result = advertiserService.findAdvertisersByName("Test")
        result shouldBe advertisers

        coVerify(exactly = 1) { advertiserRepo.findByName("Test") }
    }

    "should return all advertisers" {
        val advertisers = listOf(Advertiser(UUID.randomUUID(), "Test Advertiser"))
        coEvery { advertiserRepo.getAll() } returns advertisers

        val result = advertiserService.getAllAdvertisers()
        result shouldBe advertisers

        coVerify(exactly = 1) { advertiserRepo.getAll() }
    }
})
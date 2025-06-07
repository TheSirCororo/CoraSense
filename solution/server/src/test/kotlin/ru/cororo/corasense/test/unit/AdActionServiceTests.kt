package ru.cororo.corasense.test.unit

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import ru.cororo.corasense.model.action.data.AdAction
import ru.cororo.corasense.model.action.data.AdActionStats
import ru.cororo.corasense.model.action.data.AdActionStatsDaily
import ru.cororo.corasense.repo.action.AdActionRepo
import ru.cororo.corasense.service.AdActionService
import ru.cororo.corasense.service.CurrentDayService
import ru.cororo.corasense.service.MicrometerService
import java.util.UUID

// unit тесты написаны при помощи чата гпт
class AdActionServiceTests : StringSpec({
    val adActionRepo = mockk<AdActionRepo>()
    val currentDayService = mockk<CurrentDayService>()
    val micrometerService = mockk<MicrometerService>(relaxed = true)
    val adActionService = AdActionService(adActionRepo, currentDayService, micrometerService)

    "should return total campaign stats" {
        val campaignId = UUID.randomUUID()
        val expectedStats = AdActionStats(100, 10, 0.1, 50.0, 20.0, 70.0)

        coEvery { adActionRepo.getTotalCampaignStats(campaignId) } returns expectedStats

        val result = adActionService.getTotalCampaignStats(campaignId)
        result shouldBe expectedStats

        coVerify { adActionRepo.getTotalCampaignStats(campaignId) }
    }

    "should return daily campaign stats with missing days filled" {
        val campaignId = UUID.randomUUID()
        val currentDay = 3
        val dailyStats = mapOf(
            0 to AdActionStatsDaily(10, 1, 0.1, 5.0, 2.0, 7.0, 0),
            2 to AdActionStatsDaily(20, 2, 0.2, 10.0, 4.0, 14.0, 2)
        )

        coEvery { adActionRepo.getDailyCampaignStats(campaignId) } returns dailyStats
        coEvery { currentDayService.getCurrentDay() } returns currentDay

        val result = adActionService.getDailyCampaignStats(campaignId)
        result.size shouldBe (currentDay + 1)
        result[1] shouldBe AdActionStatsDaily(0, 0, 0.0, 0.0, 0.0, 0.0, 1)
        result[3] shouldBe AdActionStatsDaily(0, 0, 0.0, 0.0, 0.0, 0.0, 3)

        coVerify { adActionRepo.getDailyCampaignStats(campaignId) }
    }

    "should save ad action and trigger micrometer updates" {
        val clientId = UUID.randomUUID()
        val campaignId = UUID.randomUUID()
        val advertiserId = UUID.randomUUID()
        val clickId = UUID.randomUUID()
        val adAction = AdAction(clickId, advertiserId, campaignId, clientId, AdAction.Type.IMPRESSION, 1, 1.0)

        coEvery { adActionRepo.save(adAction) } just Runs

        runTest { adActionService.saveAction(adAction) }

        coVerify { adActionRepo.save(adAction) }
        coVerify { micrometerService.markToUpdateCampaign(adAction.campaignId) }
        coVerify { micrometerService.markToUpdateAdvertiser(adAction.advertiserId) }
    }

    "should return whether a user has impressed a campaign" {
        val clientId = UUID.randomUUID()
        val campaignId = UUID.randomUUID()
        val advertiserId = UUID.randomUUID()
        val clickId = UUID.randomUUID()
        val adAction = AdAction(clickId, advertiserId, campaignId, clientId, AdAction.Type.IMPRESSION, 1, 1.0)

        coEvery { adActionRepo.getUserImpression(campaignId, clientId) } returns adAction

        val result = adActionService.hasImpressed(clientId, campaignId)
        result shouldBe true

        coVerify { adActionRepo.getUserImpression(campaignId, clientId) }
    }

    "should return user click if exists" {
        val clientId = UUID.randomUUID()
        val campaignId = UUID.randomUUID()
        val advertiserId = UUID.randomUUID()
        val clickId = UUID.randomUUID()
        val adAction = AdAction(clickId, advertiserId, campaignId, clientId, AdAction.Type.CLICK, 1, 1.0)

        coEvery { adActionRepo.getUserClick(campaignId, clientId) } returns adAction

        val result = adActionService.getUserClick(clientId, campaignId)
        result shouldBe adAction

        coVerify { adActionRepo.getUserClick(campaignId, clientId) }
    }

    "should check if campaign reached click limit" {
        val campaignId = UUID.randomUUID()

        coEvery { adActionRepo.reachedClickLimit(campaignId) } returns true

        val result = adActionService.reachedClickLimit(campaignId)
        result shouldBe true

        coVerify { adActionRepo.reachedClickLimit(campaignId) }
    }
})

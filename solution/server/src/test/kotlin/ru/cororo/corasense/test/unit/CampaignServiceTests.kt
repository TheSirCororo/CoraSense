package ru.cororo.corasense.test.unit

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.Runs
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import ru.cororo.corasense.model.campaign.data.Campaign
import ru.cororo.corasense.model.campaign.dto.CampaignCreateRequest
import ru.cororo.corasense.model.client.data.Client
import ru.cororo.corasense.repo.action.AdActionRepo
import ru.cororo.corasense.repo.campaign.CampaignRepo
import ru.cororo.corasense.service.CampaignService
import ru.cororo.corasense.service.CurrentDayService
import ru.cororo.corasense.service.MicrometerService
import java.util.*

// unit тесты написаны при помощи чата гпт
class CampaignServiceTests : StringSpec({
    val campaignRepo = mockk<CampaignRepo>()
    val adActionRepo = mockk<AdActionRepo>()
    val currentDayService = mockk<CurrentDayService>()
    val micrometerService = mockk<MicrometerService>()
    val campaignService = CampaignService(campaignRepo, adActionRepo, currentDayService, micrometerService)

    beforeTest {
        clearAllMocks()
    }

    val randomCampaign = Campaign(
        id = UUID.randomUUID(),
        advertiserId = UUID.randomUUID(),
        impressionsLimit = 1000,
        clicksLimit = 500,
        costPerImpression = 0.1,
        costPerClick = 0.2,
        adTitle = "Test Ad",
        adText = "Test Ad Text",
        startDate = 20230101,
        endDate = 20231231,
        imageId = null,
        targeting = Campaign.Targeting(gender = Campaign.Targeting.Gender.MALE)
    )

    val campaignCreateRequest = CampaignCreateRequest(
        impressionsLimit = 1000,
        clicksLimit = 500,
        costPerImpression = 0.1,
        costPerClick = 0.2,
        adTitle = "Test Ad",
        adText = "Test Ad Text",
        startDate = 20230101,
        endDate = 20231231,
        targeting = CampaignCreateRequest.Targeting(gender = Campaign.Targeting.Gender.MALE)
    )

    "should create campaign" {
        val advertiserId = UUID.randomUUID()
        coEvery { campaignRepo.createCampaign(advertiserId, campaignCreateRequest) } returns randomCampaign

        val result = campaignService.createCampaign(advertiserId, campaignCreateRequest)

        result shouldBe randomCampaign
        coVerify(exactly = 1) { campaignRepo.createCampaign(advertiserId, campaignCreateRequest) }
    }

    "should save campaign" {
        coEvery { campaignRepo.save(randomCampaign) } just Runs
        coEvery { micrometerService.markToUpdateCampaign(randomCampaign.id) } just Runs
        coEvery { micrometerService.markToUpdateAdvertiser(randomCampaign.advertiserId) } just Runs

        campaignService.saveCampaign(randomCampaign)

        coVerify(exactly = 1) { campaignRepo.save(randomCampaign) }
        coVerify(exactly = 1) { micrometerService.markToUpdateCampaign(randomCampaign.id) }
        coVerify(exactly = 1) { micrometerService.markToUpdateAdvertiser(randomCampaign.advertiserId) }
    }

    "should return campaign by ID" {
        coEvery { campaignRepo.get(randomCampaign.id) } returns randomCampaign

        val result = campaignService.getCampaign(randomCampaign.id)

        result shouldBe randomCampaign
        coVerify(exactly = 1) { campaignRepo.get(randomCampaign.id) }
    }

    "should return advertiser campaigns with offset and limit" {
        val campaigns = setOf(randomCampaign)
        coEvery { campaignRepo.getAdvertiserCampaigns(randomCampaign.advertiserId, 0, 10) } returns (campaigns to 1L)

        val result = campaignService.getAdvertiserCampaigns(randomCampaign.advertiserId, 0, 10)

        result.first shouldBe campaigns
        result.second shouldBe 1L
        coVerify(exactly = 1) { campaignRepo.getAdvertiserCampaigns(randomCampaign.advertiserId, 0, 10) }
    }

    "should delete campaign" {
        coEvery { campaignRepo.get(randomCampaign.id) } returns randomCampaign
        coEvery { adActionRepo.deleteAllByCampaignId(randomCampaign.id) } just Runs
        coEvery { campaignRepo.delete(randomCampaign.id) } just Runs
        coEvery { micrometerService.deleteCampaignStats(randomCampaign.id) } just Runs
        coEvery { micrometerService.markToUpdateAdvertiser(randomCampaign.advertiserId) } just Runs

        campaignService.deleteCampaign(randomCampaign.id)

        coVerify(exactly = 1) { campaignRepo.get(randomCampaign.id) }
        coVerify(exactly = 1) { adActionRepo.deleteAllByCampaignId(randomCampaign.id) }
        coVerify(exactly = 1) { campaignRepo.delete(randomCampaign.id) }
        coVerify(exactly = 1) { micrometerService.deleteCampaignStats(randomCampaign.id) }
        coVerify(exactly = 1) { micrometerService.markToUpdateAdvertiser(randomCampaign.advertiserId) }
    }

    "should return relevant campaign for client" {
        val client = mockk<Client>()
        coEvery { currentDayService.getCurrentDay() } returns 20230115
        coEvery { campaignRepo.getRelevantCampaignForClient(client, 20230115) } returns randomCampaign

        val result = campaignService.getRelevantCampaignForClient(client)
        result shouldBe randomCampaign

        coVerify(exactly = 1) { campaignRepo.getRelevantCampaignForClient(client, 20230115) }
    }

    "should return all campaigns" {
        val campaigns = listOf(randomCampaign)
        coEvery { campaignRepo.getAll() } returns campaigns

        val result = campaignService.getAllCampaigns()

        result shouldBe campaigns
        coVerify(exactly = 1) { campaignRepo.getAll() }
    }
})

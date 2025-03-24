package ru.cororo.corasense.test.unit

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.ktor.test.dispatcher.*
import io.mockk.Runs
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import ru.cororo.corasense.model.advertiser.data.Advertiser
import ru.cororo.corasense.model.campaign.data.Campaign
import ru.cororo.corasense.repo.advertiser.AdvertiserRepo
import ru.cororo.corasense.repo.campaign.CampaignRepo
import ru.cororo.corasense.repo.time.CurrentDayTable
import ru.cororo.corasense.service.CurrentDayService
import ru.cororo.corasense.service.MicrometerService
import java.util.UUID

// unit тесты написаны при помощи чата гпт
class CurrentDayServiceTests : StringSpec({
    val campaignRepo = mockk<CampaignRepo>(relaxed = true)
    val advertiserRepo = mockk<AdvertiserRepo>(relaxed = true)
    val micrometerService = mockk<MicrometerService>(relaxed = true)

    Database.connect("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;", driver = "org.h2.Driver")
    beforeTest {
        clearAllMocks()
        startKoin {
            modules(
                module {
                    single { micrometerService }
                    single { campaignRepo }
                    single { advertiserRepo }
                }
            )
        }
    }

    afterTest {
        stopKoin()
    }

    transaction {
        SchemaUtils.create(CurrentDayTable)
    }

    "should return 0 if no current day is set" {
        testSuspend {
            CurrentDayService().getCurrentDay() shouldBe 0
        }
    }

    "should return the current day from DB" {
        coEvery { advertiserRepo.getAll() } returns listOf()
        coEvery { campaignRepo.getAll() } returns listOf()

        testSuspend {
            val service = CurrentDayService()
            service.setCurrentDay(5)
            service.getCurrentDay() shouldBe 5
        }
    }

    "should update current day and notify services" {
        val testAdvertiser = Advertiser(UUID.randomUUID(), "test")
        val testCampaign = Campaign(UUID.randomUUID(), testAdvertiser.id, 10, 10, 1.0, 1.0, "test", "test", 1, 100, null, Campaign.Targeting())
        coEvery { micrometerService.markToUpdateCampaign(any()) } just Runs
        coEvery { micrometerService.markToUpdateAdvertiser(any()) } just Runs
        coEvery { advertiserRepo.getAll() } returns listOf(testAdvertiser)
        coEvery { campaignRepo.getAll() } returns listOf(testCampaign)

        testSuspend {
            val service = CurrentDayService()
            service.setCurrentDay(10)
        }

        transaction {
            CurrentDayTable.selectAll().single()[CurrentDayTable.currentDay] shouldBe 10
        }

        coVerify { micrometerService.markToUpdateCampaign(any()) }
        coVerify { micrometerService.markToUpdateAdvertiser(any()) }
        coVerify { advertiserRepo.getAll() }
        coVerify { campaignRepo.getAll() }
    }
})

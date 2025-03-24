package ru.cororo.corasense.service

import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import ru.cororo.corasense.plugin.sql
import ru.cororo.corasense.repo.advertiser.AdvertiserRepo
import ru.cororo.corasense.repo.campaign.CampaignRepo
import ru.cororo.corasense.repo.time.CurrentDayTable

class CurrentDayService : KoinComponent {
    private val micrometerService by inject<MicrometerService>()
    private val campaignRepo by inject<CampaignRepo>()
    private val advertiserRepo by inject<AdvertiserRepo>()
    private var currentDay: Int? = null

    suspend fun getCurrentDay() = currentDay ?: sql {
        this@CurrentDayService.currentDay = CurrentDayTable.selectAll().singleOrNull()?.getOrNull(CurrentDayTable.currentDay) ?: 0
        currentDay!!
    }

    suspend fun setCurrentDay(day: Int) = sql {
        if ((currentDay == null || currentDay == 0) && CurrentDayTable.selectAll().count() == 0L) {
            CurrentDayTable.insert {
                it[CurrentDayTable.currentDay] = day
            }
        } else {
            CurrentDayTable.update {
                it[CurrentDayTable.currentDay] = day
            }
        }

        this@CurrentDayService.currentDay = day
        for (campaign in campaignRepo.getAll()) {
            micrometerService.markToUpdateCampaign(campaign.id)
        }

        for (advertiser in advertiserRepo.getAll()) {
            micrometerService.markToUpdateAdvertiser(advertiser.id)
        }
    }
}
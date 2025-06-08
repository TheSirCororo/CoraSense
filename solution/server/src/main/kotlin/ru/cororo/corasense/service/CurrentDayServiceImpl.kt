package ru.cororo.corasense.service

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import ru.cororo.corasense.plugin.sql
import ru.cororo.corasense.repo.advertiser.AdvertiserRepo
import ru.cororo.corasense.repo.campaign.CampaignRepo
import ru.cororo.corasense.repo.time.CurrentDayTable
import ru.cororo.corasense.shared.service.CurrentDayService
import kotlin.coroutines.CoroutineContext

class CurrentDayServiceImpl : CurrentDayService, KoinComponent {

    override val coroutineContext: CoroutineContext = Dispatchers.Default + CoroutineName("CurrentDayServiceImpl")

    private val micrometerService by inject<MicrometerService>()
    private val campaignRepo by inject<CampaignRepo>()
    private val advertiserRepo by inject<AdvertiserRepo>()
    private var currentDay: Int? = null

    override suspend fun getCurrentDay() = currentDay ?: sql {
        this@CurrentDayServiceImpl.currentDay =
            CurrentDayTable.selectAll().singleOrNull()?.getOrNull(CurrentDayTable.currentDay) ?: 0
        currentDay!!
    }

    override suspend fun setCurrentDay(day: Int) = sql {
        if ((currentDay == null || currentDay == 0) && CurrentDayTable.selectAll().count() == 0L) {
            CurrentDayTable.insert {
                it[CurrentDayTable.currentDay] = day
            }
        } else {
            CurrentDayTable.update {
                it[CurrentDayTable.currentDay] = day
            }
        }

        this@CurrentDayServiceImpl.currentDay = day
        for (campaign in campaignRepo.getAll()) {
            micrometerService.markToUpdateCampaign(campaign.id)
        }

        for (advertiser in advertiserRepo.getAll()) {
            micrometerService.markToUpdateAdvertiser(advertiser.id)
        }
    }
}
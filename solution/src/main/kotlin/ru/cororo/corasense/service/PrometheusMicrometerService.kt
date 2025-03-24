package ru.cororo.corasense.service

import io.ktor.server.application.*
import io.micrometer.core.instrument.Meter
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tag
import kotlinx.coroutines.*
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import ru.cororo.corasense.model.action.data.AdActionStats
import ru.cororo.corasense.model.action.data.AdActionStatsDaily
import ru.cororo.corasense.model.advertiser.data.Advertiser
import ru.cororo.corasense.model.campaign.data.Campaign
import java.util.UUID
import java.util.concurrent.atomic.AtomicReference
import kotlin.collections.ArrayDeque
import kotlin.collections.List
import kotlin.collections.any
import kotlin.collections.filter
import kotlin.collections.forEach
import kotlin.collections.getOrPut
import kotlin.collections.listOf
import kotlin.collections.mutableListOf
import kotlin.collections.mutableMapOf
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration.Companion.seconds

object PrometheusMicrometerService : CoroutineScope, KoinComponent, MicrometerService {
    private lateinit var meterRegistry: MeterRegistry
    private val application = get<Application>()
    private val gauges = mutableMapOf<Pair<String, List<Tag>>, AtomicReference<Double>>()
    private val markedCampaigns = ArrayDeque<UUID>()
    private val markedAdvertisers = ArrayDeque<UUID>()
    override val coroutineContext: CoroutineContext = Dispatchers.IO + CoroutineName("MicrometerService") + Job()

    override fun init(registry: MeterRegistry) {
        meterRegistry = registry

        val campaignService = get<CampaignService>()
        val advertiserService = get<AdvertiserService>()
        val adActionService = get<AdActionService>()
        launch {
            campaignService.getAllCampaigns().forEach { campaign ->
                updateCampaignStats(campaign, adActionService.getTotalCampaignStats(campaign.id))
                adActionService.getDailyCampaignStats(campaign.id).forEach {
                    updateDailyCampaignStats(campaign, it)
                }
            }

            advertiserService.getAllAdvertisers().forEach { advertiser ->
                updateAdvertiserStats(advertiser, adActionService.getTotalAdvertiserStats(advertiser.id))
                adActionService.getDailyAdvertiserStats(advertiser.id).forEach {
                    updateDailyAdvertiserStats(advertiser, it)
                }
            }
        }

        launch {
            while (true) {
                delay(10.seconds)

                var campaignId = markedCampaigns.removeLastOrNull()
                while (campaignId != null) {
                    val campaign = campaignService.getCampaign(campaignId)
                    if (campaign != null) {
                        updateCampaignStats(campaign, adActionService.getTotalCampaignStats(campaignId))
                        adActionService.getDailyCampaignStats(campaignId).forEach {
                            updateDailyCampaignStats(campaign, it)
                        }
                    }

                    campaignId = markedCampaigns.removeLastOrNull()
                }

                var advertiserId = markedAdvertisers.removeLastOrNull()
                while (advertiserId != null) {
                    val advertiser = advertiserService.getAdvertiser(advertiserId)
                    if (advertiser != null) {
                        updateAdvertiserStats(advertiser, adActionService.getTotalAdvertiserStats(advertiserId))
                        adActionService.getDailyAdvertiserStats(advertiserId).forEach {
                            updateDailyAdvertiserStats(advertiser, it)
                        }
                    }

                    advertiserId = markedCampaigns.removeLastOrNull()
                }
            }
        }

        application.monitor.subscribe(ApplicationStopped) {
            cancel()
        }
    }

    override fun updateCampaignStats(campaign: Campaign, stats: AdActionStats) {
        val tags = listOf(Tag.of("campaign_id", campaign.id.toString()), Tag.of("campaign_title", campaign.adTitle))
        getGauge("campaign_total_impressions_count", tags).set(stats.impressionsCount.toDouble())
        getGauge("campaign_total_clicks_count", tags).set(stats.clicksCount.toDouble())
        getGauge("campaign_total_spent_impressions", tags).set(stats.spentImpressions)
        getGauge("campaign_total_spent_clicks", tags).set(stats.spentClicks)
        getGauge("campaign_total_spent_total", tags).set(stats.spentTotal)
        getGauge("campaign_total_conversion", tags).set(stats.conversion)
    }

    override fun updateAdvertiserStats(advertiser: Advertiser, stats: AdActionStats) {
        val tags = listOf(Tag.of("advertiser_id", advertiser.id.toString()), Tag.of("advertiser_name", advertiser.name))
        getGauge("advertiser_total_impressions_count", tags).set(stats.impressionsCount.toDouble())
        getGauge("advertiser_total_clicks_count", tags).set(stats.clicksCount.toDouble())
        getGauge("advertiser_total_spent_impressions", tags).set(stats.spentImpressions)
        getGauge("advertiser_total_spent_clicks", tags).set(stats.spentClicks)
        getGauge("advertiser_total_spent_total", tags).set(stats.spentTotal)
        getGauge("advertiser_total_conversion", tags).set(stats.conversion)
    }

    override fun updateDailyCampaignStats(campaign: Campaign, stats: AdActionStatsDaily) {
        val tags = listOf(
            Tag.of("campaign_id", campaign.id.toString()),
            Tag.of("date", stats.date.toString()),
            Tag.of("campaign_title", campaign.adTitle)
        )
        getGauge("campaign_daily_impressions_count", tags).set(stats.impressionsCount.toDouble())
        getGauge("campaign_daily_clicks_count", tags).set(stats.clicksCount.toDouble())
        getGauge("campaign_daily_spent_impressions", tags).set(stats.spentImpressions)
        getGauge("campaign_daily_spent_clicks", tags).set(stats.spentClicks)
        getGauge("campaign_daily_spent_total", tags).set(stats.spentTotal)
        getGauge("campaign_daily_conversion", tags).set(stats.conversion)
    }

    override fun updateDailyAdvertiserStats(advertiser: Advertiser, stats: AdActionStatsDaily) {
        val tags = listOf(
            Tag.of("advertiser_id", advertiser.id.toString()),
            Tag.of("date", stats.date.toString()),
            Tag.of("advertiser_name", advertiser.name)
        )
        getGauge("advertiser_daily_impressions_count", tags).set(stats.impressionsCount.toDouble())
        getGauge("advertiser_daily_clicks_count", tags).set(stats.clicksCount.toDouble())
        getGauge("advertiser_daily_spent_impressions", tags).set(stats.spentImpressions)
        getGauge("advertiser_daily_spent_clicks", tags).set(stats.spentClicks)
        getGauge("advertiser_daily_spent_total", tags).set(stats.spentTotal)
        getGauge("advertiser_daily_conversion", tags).set(stats.conversion)
    }

    override suspend fun markToUpdateCampaign(campaignId: UUID) {
        markedCampaigns.add(campaignId)
    }

    override suspend fun markToUpdateAdvertiser(advertiserId: UUID) {
        markedAdvertisers.add(advertiserId)
    }

    override suspend fun deleteCampaignStats(campaignId: UUID) {
        application.log.debug("Удаляем статистику кампании {}...", campaignId)
        gauges.filter { it.key.first.startsWith("campaign") && it.key.second.any { it.value == campaignId.toString() } }
            .forEach {
                gauges.remove(it.key)
            }

        val metersToRemove = mutableListOf<Meter>()
        meterRegistry.forEachMeter {
            val id = it.id
            if (id.name.startsWith("campaign") && id.tags.any { it.value == campaignId.toString() }) {
                metersToRemove.add(it)
            }
        }

        for (meter in metersToRemove) {
            meterRegistry.remove(meter)
        }
    }

    override suspend fun deleteAdvertiserStats(advertiserId: UUID) {
        gauges.filter { it.key.first.startsWith("advertiser") && it.key.second.any { it.value == advertiserId.toString() } }
            .forEach {
                gauges.remove(it.key)
            }

        val metersToRemove = mutableListOf<Meter>()
        meterRegistry.forEachMeter {
            val id = it.id
            if (id.name.startsWith("advertiser") && id.tags.any { it.value == advertiserId.toString() }) {
                metersToRemove.add(it)
            }
        }

        for (meter in metersToRemove) {
            meterRegistry.remove(meter)
        }
    }

    private fun getGauge(name: String, tags: List<Tag>) = gauges.getOrPut(name to tags) {
        meterRegistry.gauge(name, tags, AtomicReference(0.0)) { it.get() }
    }
}
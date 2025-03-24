package ru.cororo.corasense.route.stats

import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import org.koin.core.component.get
import ru.cororo.corasense.inject.api
import ru.cororo.corasense.model.action.data.AdActionStats
import ru.cororo.corasense.model.action.data.AdActionStatsDaily
import ru.cororo.corasense.model.dto.Errors
import ru.cororo.corasense.model.dto.StatusResponse
import ru.cororo.corasense.model.dto.respond
import ru.cororo.corasense.route.Paths
import ru.cororo.corasense.service.AdActionService
import ru.cororo.corasense.service.AdvertiserService
import ru.cororo.corasense.service.CampaignService
import ru.cororo.corasense.util.getUuid
import ru.cororo.corasense.util.get
import java.util.UUID

fun Route.statsApi() = api {
    val adActionService = it.get<AdActionService>()
    val campaignService = it.get<CampaignService>()
    val advertiserService = it.get<AdvertiserService>()

    get<Paths.Stats.Advertisers>({
        summary = "Общая статистика рекламодателя"
        description = "Возвращает сводную статистику по всем рекламным кампаниям, принадлежащим заданному рекламодателю."

        request {
            pathParameter<UUID>("advertiserId") {
                required = true
                description = "ID рекламодателя"
            }
        }

        response {
            code(HttpStatusCode.OK) {
                body<AdActionStats> {
                    example("Test stats") {
                        value = AdActionStats(10, 10, 100.0, 20.0, 40.0, 60.0)
                    }
                }
            }

            code(HttpStatusCode.NotFound) {
                body<StatusResponse.Error> {
                    example("Advertiser not found") {
                        value = Errors.AdvertiserNotFound
                    }
                }
            }
        }
    }) {
        val advertiserId = call.getUuid(it.advertiserId)
        advertiserService.getAdvertiser(advertiserId) ?: Errors.AdvertiserNotFound.respond()
        call.respond(adActionService.getTotalAdvertiserStats(advertiserId))
    }

    get<Paths.Stats.Advertisers.Daily>({
        summary = "Ежедневная статистика рекламодателя"
        description = "Возвращает массив ежедневной сводной статистики по всем рекламным кампаниям заданного рекламодателя."

        request {
            pathParameter<UUID>("advertiserId") {
                required = true
                description = "ID рекламодателя"
            }
        }

        response {
            code(HttpStatusCode.OK) {
                body<List<AdActionStatsDaily>> {
                    example("Test stats") {
                        value = listOf(AdActionStatsDaily(10, 10, 100.0, 20.0, 40.0, 60.0, 1))
                    }
                }
            }

            code(HttpStatusCode.NotFound) {
                body<StatusResponse.Error> {
                    example("Advertiser not found") {
                        value = Errors.AdvertiserNotFound
                    }
                }
            }
        }
    }) {
        val advertiserId = call.getUuid(it.advertiserId)
        advertiserService.getAdvertiser(advertiserId) ?: Errors.AdvertiserNotFound.respond()
        call.respond(adActionService.getDailyAdvertiserStats(advertiserId))
    }

    get<Paths.Stats.Campaigns>({
        summary = "Общая статистика рекламной кампании"
        description = "Возвращает агрегированную статистику (показы, переходы, затраты и конверсию) для заданной рекламной кампании."

        request {
            pathParameter<UUID>("campaignId") {
                required = true
                description = "ID рекламной кампании"
            }
        }

        response {
            code(HttpStatusCode.OK) {
                body<AdActionStats> {
                    example("Test stats") {
                        value = AdActionStats(10, 10, 100.0, 20.0, 40.0, 60.0)
                    }
                }
            }

            code(HttpStatusCode.NotFound) {
                body<StatusResponse.Error> {
                    example("Campaign not found") {
                        value = Errors.CampaignNotFound
                    }
                }
            }
        }
    }) {
        val campaignId = call.getUuid(it.campaignId)
        campaignService.getCampaign(campaignId) ?: Errors.CampaignNotFound.respond()
        call.respond(adActionService.getTotalCampaignStats(campaignId))
    }

    get<Paths.Stats.Campaigns.Daily>({
        summary = "Ежедневная статистика рекламной кампании"
        description = "Возвращает массив ежедневной сводной статистики по указанной рекламной кампании."

        request {
            pathParameter<UUID>("campaignId") {
                required = true
                description = "ID рекламной кампании"
            }
        }

        response {
            code(HttpStatusCode.OK) {
                body<List<AdActionStatsDaily>> {
                    example("Test stats") {
                        value = listOf(AdActionStatsDaily(10, 10, 100.0, 20.0, 40.0, 60.0, 1))
                    }
                }
            }

            code(HttpStatusCode.NotFound) {
                body<StatusResponse.Error> {
                    example("Campaign not found") {
                        value = Errors.CampaignNotFound
                    }
                }
            }
        }
    }) {
        val campaignId = call.getUuid(it.campaignId)
        campaignService.getCampaign(campaignId) ?: Errors.CampaignNotFound.respond()
        call.respond(adActionService.getDailyCampaignStats(campaignId))
    }
}
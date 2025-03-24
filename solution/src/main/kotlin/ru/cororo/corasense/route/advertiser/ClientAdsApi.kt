package ru.cororo.corasense.route.advertiser

import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.core.component.get
import ru.cororo.corasense.inject.api
import ru.cororo.corasense.model.action.data.AdAction
import ru.cororo.corasense.model.campaign.dto.CampaignAd
import ru.cororo.corasense.model.campaign.dto.CampaignClickRequest
import ru.cororo.corasense.model.dto.Errors
import ru.cororo.corasense.model.dto.StatusResponse
import ru.cororo.corasense.model.dto.respond
import ru.cororo.corasense.route.Paths
import ru.cororo.corasense.service.AdActionService
import ru.cororo.corasense.service.CampaignService
import ru.cororo.corasense.service.ClientService
import ru.cororo.corasense.service.CurrentDayService
import ru.cororo.corasense.util.get
import ru.cororo.corasense.util.getUuid
import ru.cororo.corasense.util.post
import java.util.*

fun Route.clientAdsApi() = api {
    val campaignService = it.get<CampaignService>()
    val currentDayService = it.get<CurrentDayService>()
    val clientService = it.get<ClientService>()
    val adActionService = it.get<AdActionService>()

    get<Paths.Ads>({
        summary = "Просмотр рекламы пользователем"

        request {
            queryParameter<UUID>("client_id") {
                description = "ID клиента"
                required = true
            }
        }

        response {
            code(HttpStatusCode.OK) {
                body<CampaignAd>()
            }

            code(HttpStatusCode.BadRequest) {
                body<StatusResponse.Error> {
                    example("Bad request") {
                        value = Errors.BadRequest
                    }
                }
            }

            code(HttpStatusCode.NotFound) {
                body<StatusResponse.Error> {
                    example("Client not found") {
                        value = Errors.ClientNotFound
                    }

                    example("No ads") {
                        value = Errors.NoAds
                    }
                }
            }
        }
    }) {
        val clientId = call.getUuid(call.queryParameters["client_id"] ?: Errors.BadRequest.respond())
        val client = clientService.getClient(clientId) ?: Errors.ClientNotFound.respond()
        val campaign = campaignService.getRelevantCampaignForClient(client) ?: Errors.NoAds.respond()
        adActionService.saveAction(
            AdAction(
                UUID.randomUUID(),
                campaign.advertiserId,
                campaign.id,
                clientId,
                AdAction.Type.IMPRESSION,
                currentDayService.getCurrentDay(),
                campaign.costPerImpression
            )
        )

        call.respond(
            CampaignAd(
                campaign.id,
                campaign.adTitle,
                campaign.adText,
                campaign.advertiserId,
                campaign.imageId
            )
        )
    }

    post<Paths.Ads.Click>({
        summary = "Кликнуть по рекламе"

        request {
            pathParameter<String>("adId") {
                required = true
                description = "ID рекламы"
            }

            body<CampaignClickRequest>()
        }

        response {
            code(HttpStatusCode.NoContent) {}

            code(HttpStatusCode.NotFound) {
                body<StatusResponse.Error> {
                    example("Ad not found") {
                        value = Errors.CampaignNotFound
                    }
                }

                body<StatusResponse.Error> {
                    example("Client not found") {
                        value = Errors.CampaignNotFound
                    }
                }
            }

            code(HttpStatusCode.Forbidden) {
                body<StatusResponse.Error> {
                    example("Ad forbidden (user must see it first)") {
                        value = Errors.AdForbidden
                    }
                }
            }
        }
    }) {
        val adId = call.getUuid(it.adId)
        val (clientId) = call.receive<CampaignClickRequest>()
        val campaign = campaignService.getCampaign(adId) ?: Errors.CampaignNotFound.respond()
        clientService.getClient(clientId) ?: Errors.ClientNotFound.respond()
        if (!adActionService.hasImpressed(clientId, campaign.id)) Errors.AdForbidden.respond()
        adActionService.getUserClick(clientId, campaign.id) ?: adActionService.saveAction(
            AdAction(
                UUID.randomUUID(),
                campaign.advertiserId,
                campaign.id,
                clientId,
                AdAction.Type.CLICK,
                currentDayService.getCurrentDay(),
                campaign.costPerClick
            )
        )

        call.respondText("", status = HttpStatusCode.NoContent)
    }
}
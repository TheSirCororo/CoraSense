package ru.cororo.corasense.route.advertiser

import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.core.component.get
import ru.cororo.corasense.inject.api
import ru.cororo.corasense.model.campaign.data.Campaign
import ru.cororo.corasense.model.campaign.data.hasStarted
import ru.cororo.corasense.model.campaign.dto.CampaignCreateRequest
import ru.cororo.corasense.model.campaign.dto.CampaignUpdateRequest
import ru.cororo.corasense.model.dto.Errors
import ru.cororo.corasense.model.dto.StatusResponse
import ru.cororo.corasense.model.dto.respond
import ru.cororo.corasense.model.moderation.data.ModerationScope
import ru.cororo.corasense.route.Paths
import ru.cororo.corasense.service.*
import ru.cororo.corasense.util.*

fun Route.campaignApi() = api {
    val advertiserService = it.get<AdvertiserService>()
    val campaignService = it.get<CampaignService>()
    val currentDayService = it.get<CurrentDayService>()
    val imageService = it.get<ImageService>()
    val moderationService = it.get<ModerationService>()

    post<Paths.Advertisers.ById.Campaigns>({
        summary = "Создание рекламной кампании"
        request {
            pathParameter<String>("advertiserId") {
                required = true
                description = "ID рекламодателя"
            }

            body<CampaignCreateRequest>()
        }

        response {
            code(HttpStatusCode.OK) {
                body<Campaign>()
            }

            code(HttpStatusCode.BadRequest) {
                body<StatusResponse.Error> {
                    example("Error") {
                        value = Errors.BadRequest
                    }
                }
            }
        }
    }) {
        val advertiserId = call.getUuid(it.advertiserId)
        val request = call.receive<CampaignCreateRequest>()
        val currentDay = currentDayService.getCurrentDay()
        if (request.startDate < currentDay || request.endDate < currentDay) {
            Errors.OutdatedDate.respond()
        }

        moderationService.moderateIfNeed(ModerationScope.AD_TITLE, request.adTitle).let {
            if (!it.allowed) {
                Errors.ModerationRejected(it).respond()
            }
        }

        moderationService.moderateIfNeed(ModerationScope.AD_TEXT, request.adText).let {
            if (!it.allowed) {
                Errors.ModerationRejected(it).respond()
            }
        }

        advertiserService.getAdvertiser(advertiserId) ?: Errors.AdvertiserNotFound.respond()

        request.imageId?.let {
            if (imageService.getImage(it) == null) {
                Errors.ImageNotFound.respond()
            }
        }

        val campaign = campaignService.createCampaign(advertiserId, request)
        call.respond(HttpStatusCode.Created, campaign)
    }

    get<Paths.Advertisers.ById.Campaigns>({
        summary = "Получение информации о рекламных кампаниях рекламодателя"
        request {
            pathParameter<String>("advertiserId") {
                required = true
                description = "ID рекламодателя"
            }
        }

        response {
            code(HttpStatusCode.OK) {
                body<List<Campaign>>()
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
        val parameters = call.queryParameters
        val pageSize = parameters["size"]?.let { it.toIntOrNull() ?: Errors.BadRequest.respond() } ?: Int.MAX_VALUE
        val pageNum = parameters["page"]?.let { it.toLongOrNull() ?: Errors.BadRequest.respond() } ?: 1
        advertiserService.getAdvertiser(advertiserId) ?: Errors.AdvertiserNotFound.respond()

        if (pageSize == 0) {
            call.respond(emptyList<Campaign>())
            return@get
        }

        val offset = pageSize * (pageNum - 1)
        call.respond(campaignService.getAdvertiserCampaigns(advertiserId, offset, pageSize).first)
    }

    get<Paths.Advertisers.ById.Campaigns.CampaignById>({
        summary = "Получение информации о рекламной кампании"
        request {
            pathParameter<String>("advertiserId") {
                required = true
                description = "ID рекламодателя"
            }

            pathParameter<String>("campaignId") {
                required = true
                description = "ID кампании"
            }
        }

        response {
            code(HttpStatusCode.OK) {
                body<Campaign>()
            }

            code(HttpStatusCode.NotFound) {
                body<StatusResponse.Error> {
                    example("Advertiser not found") {
                        value = Errors.AdvertiserNotFound
                    }

                    example("Campaign not found") {
                        value = Errors.CampaignNotFound
                    }
                }
            }
        }
    }) {
        val advertiserId = call.getUuid(it.advertiserId)
        val campaignId = call.getUuid(it.campaignId)

        advertiserService.getAdvertiser(advertiserId) ?: Errors.AdvertiserNotFound.respond()
        val campaign = campaignService.getCampaign(campaignId) ?: Errors.CampaignNotFound.respond()
        if (campaign.advertiserId != advertiserId) {
            Errors.CampaignNotFound.respond()
        }

        call.respond(campaign)
    }

    put<Paths.Advertisers.ById.Campaigns.CampaignById>({
        summary = "Обновление информации о рекламной кампании"
        request {
            pathParameter<String>("advertiserId") {
                required = true
                description = "ID рекламодателя"
            }

            pathParameter<String>("campaignId") {
                required = true
                description = "ID кампании"
            }

            body<CampaignUpdateRequest>()
        }

        response {
            code(HttpStatusCode.OK) {
                body<Campaign>()
            }

            code(HttpStatusCode.NotFound) {
                body<StatusResponse.Error> {
                    example("Advertiser not found") {
                        value = Errors.AdvertiserNotFound
                    }

                    example("Campaign not found") {
                        value = Errors.CampaignNotFound
                    }
                }
            }
        }
    }) {
        val advertiserId = call.getUuid(it.advertiserId)
        val campaignId = call.getUuid(it.campaignId)
        val request = call.receive<CampaignUpdateRequest>()

        advertiserService.getAdvertiser(advertiserId) ?: Errors.AdvertiserNotFound.respond()
        val campaign = campaignService.getCampaign(campaignId) ?: Errors.CampaignNotFound.respond()
        if (campaign.advertiserId != advertiserId) {
            Errors.CampaignNotFound.respond()
        }

        if (campaign.hasStarted(currentDayService) && (campaign.impressionsLimit != request.impressionsLimit || campaign.clicksLimit != request.clicksLimit || campaign.startDate != request.startDate || campaign.endDate != request.endDate)) {
            Errors.BadRequest.respond()
        }

        request.imageId?.let {
            if (imageService.getImage(it) == null) {
                Errors.ImageNotFound.respond()
            }
        }

        val newImpressionsLimit = request.impressionsLimit
        val newClicksLimit = request.clicksLimit
        val newStartDate = request.startDate
        val newEndDate = request.endDate
        val newCostPerImpression = request.costPerImpression
        val newCostPerClick = request.costPerClick
        val newAdTitle = request.adTitle.apply {
            moderationService.moderateIfNeed(ModerationScope.AD_TITLE, this).let {
                if (!it.allowed) {
                    Errors.ModerationRejected(it).respond()
                }
            }
        }
        val newAdText = request.adText.apply {
            moderationService.moderateIfNeed(ModerationScope.AD_TEXT, this).let {
                if (!it.allowed) {
                    Errors.ModerationRejected(it).respond()
                }
            }
        }
        val newImageId = request.imageId
        val newTargeting = request.targeting?.let {
            Campaign.Targeting(
                it.gender,
                it.ageFrom,
                it.ageTo,
                it.location
            )
        } ?: Campaign.Targeting()

        val newCampaign = campaign.copy(
            impressionsLimit = newImpressionsLimit,
            clicksLimit = newClicksLimit,
            startDate = newStartDate,
            endDate = newEndDate,
            costPerImpression = newCostPerImpression,
            costPerClick = newCostPerClick,
            adTitle = newAdTitle,
            adText = newAdText,
            targeting = newTargeting,
            imageId = newImageId
        )

        campaignService.saveCampaign(newCampaign)
        call.respond(newCampaign)
    }

    delete<Paths.Advertisers.ById.Campaigns.CampaignById>({
        summary = "Удаление рекламной кампании"
        request {
            pathParameter<String>("advertiserId") {
                required = true
                description = "ID рекламодателя"
            }

            pathParameter<String>("campaignId") {
                required = true
                description = "ID кампании"
            }
        }

        response {
            code(HttpStatusCode.NoContent) {}
        }
    }) {
        val advertiserId = call.getUuid(it.advertiserId)
        val campaignId = call.getUuid(it.campaignId)

        advertiserService.getAdvertiser(advertiserId) ?: Errors.AdvertiserNotFound.respond()
        val campaign = campaignService.getCampaign(campaignId) ?: Errors.CampaignNotFound.respond()
        if (campaign.advertiserId != advertiserId) {
            Errors.CampaignNotFound.respond()
        }

        campaignService.deleteCampaign(campaignId)
        call.respondText("", status = HttpStatusCode.NoContent)
    }
}
package ru.cororo.corasense.route.advertiser

import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.core.component.get
import ru.cororo.corasense.inject.api
import ru.cororo.corasense.model.advertiser.data.Advertiser
import ru.cororo.corasense.model.dto.Errors
import ru.cororo.corasense.model.dto.StatusResponse
import ru.cororo.corasense.model.dto.respond
import ru.cororo.corasense.model.dto.respondOk
import ru.cororo.corasense.model.ml.data.MLScore
import ru.cororo.corasense.model.moderation.data.ModerationScope
import ru.cororo.corasense.model.moderation.data.ModerationVerdict
import ru.cororo.corasense.route.Paths
import ru.cororo.corasense.service.AdvertiserService
import ru.cororo.corasense.service.ClientService
import ru.cororo.corasense.service.MLScoreService
import ru.cororo.corasense.service.ModerationService
import ru.cororo.corasense.util.get
import ru.cororo.corasense.util.getUuid
import ru.cororo.corasense.util.post

fun Route.advertiserApi() = api {
    val moderationService = it.get<ModerationService>()
    val advertiserService = it.get<AdvertiserService>()
    val mlScoreService = it.get<MLScoreService>()
    val clientService = it.get<ClientService>()

    get<Paths.Advertisers.ById>({
        summary = "Получение информации о рекламодателе по его ID"
        request {
            pathParameter<String>("advertiserId") {
                required = true
                description = "ID рекламодателя"
            }
        }

        response {
            code(HttpStatusCode.OK) {
                body<Advertiser>()
            }

            code(HttpStatusCode.NotFound) {
                body<StatusResponse.Error> {
                    example("Error") {
                        value = Errors.AdvertiserNotFound
                    }
                }
            }
        }
    }) {
        val advertiserId = call.getUuid(it.advertiserId)
        val advertiser = advertiserService.getAdvertiser(advertiserId) ?: Errors.AdvertiserNotFound.respond()
        call.respond(advertiser)
    }

    post<Paths.Advertisers.Bulk>({
        summary = "Создание/сохранение информации о рекламодателях"
        request {
            body<List<Advertiser>>()
        }

        response {
            code(HttpStatusCode.OK) {
                body<List<Advertiser>>()
            }

            code(HttpStatusCode.BadRequest) {
                body<StatusResponse.Error> {
                    example("Bad body") {
                        value = Errors.BadRequest
                    }

                    example("Moderation") {
                        value = Errors.ModerationRejected(ModerationVerdict("Отклонено модерацией.", false))
                    }
                }
            }
        }
    }) {
        val body = call.receive<List<Advertiser>>()
        body.forEach {
            moderationService.moderateIfNeed(ModerationScope.ADVERTISER_NAME, it.name).let {
                if (!it.allowed) {
                    Errors.ModerationRejected(it).respond()
                }
            }
        }

        advertiserService.saveAdvertisers(body)
        call.respond(HttpStatusCode.Created, body)
    }

    post<Paths.MLScores>({
        summary = "Создание/сохранение ML скора"
        request {
            body<MLScore> {
                example("OK") {
                    value = StatusResponse.Ok()
                }
            }
        }

        response {
            code(HttpStatusCode.OK) {
                body<StatusResponse.Ok>()
            }

            code(HttpStatusCode.NotFound) {
                description = "Клиент или рекламодатель не найдены."
                body<StatusResponse.Error>()
            }
        }
    }) {
        val body = call.receive<MLScore>()
        clientService.getClient(body.clientId) ?: Errors.ClientNotFound.respond()
        advertiserService.getAdvertiser(body.advertiserId) ?: Errors.AdvertiserNotFound.respond()
        mlScoreService.saveMLScore(body)
        call.respondOk()
    }
}
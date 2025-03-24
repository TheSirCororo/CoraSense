package ru.cororo.corasense.route.client

import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.core.component.get
import ru.cororo.corasense.inject.api
import ru.cororo.corasense.model.client.data.Client
import ru.cororo.corasense.model.dto.Errors
import ru.cororo.corasense.model.dto.StatusResponse
import ru.cororo.corasense.model.dto.respond
import ru.cororo.corasense.model.moderation.data.ModerationScope
import ru.cororo.corasense.model.moderation.data.ModerationVerdict
import ru.cororo.corasense.route.Paths
import ru.cororo.corasense.service.ClientService
import ru.cororo.corasense.service.ModerationService
import ru.cororo.corasense.util.get
import ru.cororo.corasense.util.getUuid
import ru.cororo.corasense.util.post

fun Route.clientApi() = api {
    val clientService = it.get<ClientService>()
    val moderationService = it.get<ModerationService>()

    get<Paths.Clients.ById>({
        summary = "Получение информации о клиенте по его ID"
        request {
            pathParameter<String>("clientId") {
                required = true
                description = "ID клиента"
            }
        }

        response {
            code(HttpStatusCode.OK) {
                body<Client>()
            }

            code(HttpStatusCode.NotFound) {
                body<StatusResponse.Error> {
                    example("Error") {
                        value = Errors.ClientNotFound
                    }
                }
            }
        }
    }) {
        val clientId = call.getUuid(it.clientId)
        val client = clientService.getClient(clientId) ?: Errors.ClientNotFound.respond()
        call.respond(client)
    }

    post<Paths.Clients.Bulk>({
        summary = "Создание/сохранение информации о клиентах"
        request {
            body<List<Client>>()
        }

        response {
            code(HttpStatusCode.OK) {
                body<List<Client>>()
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
        val body = call.receive<List<Client>>()
        body.forEach {
            moderationService.moderateIfNeed(ModerationScope.CLIENT_LOGIN, it.login).let {
                if (!it.allowed) {
                    Errors.ModerationRejected(it).respond()
                }
            }
        }

        clientService.saveClients(body)
        call.respond(HttpStatusCode.Created, body)
    }
}
package ru.cororo.corasense.route.moderation

import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.routing.Route
import io.ktor.server.response.respond
import org.koin.core.component.get
import ru.cororo.corasense.inject.api
import ru.cororo.corasense.model.dto.StatusResponse
import ru.cororo.corasense.model.dto.respondOk
import ru.cororo.corasense.model.moderation.dto.BlockedWordsRequest
import ru.cororo.corasense.repo.moderation.BlockedWordRepo
import ru.cororo.corasense.route.Paths
import ru.cororo.corasense.util.get
import ru.cororo.corasense.util.post
import ru.cororo.corasense.util.delete

fun Route.blockedWordsApi() = api {
    val blockedWordRepo = it.get<BlockedWordRepo>()

    post<Paths.BlockedWords>({
        summary = "Добавить плохие слова в приложение для режима модерации BLACKLIST."
        request {
            body<BlockedWordsRequest> {
                example("OK") {
                    value = BlockedWordsRequest(listOf("п*тон"))
                }
            }
        }

        response {
            code(HttpStatusCode.OK) {
                body<StatusResponse.Ok> {
                    example("OK") {
                        value = StatusResponse.Ok()
                    }
                }
            }
        }
    }) {
        val (words) = call.receive<BlockedWordsRequest>()
        blockedWordRepo.saveAll(words.toSet())
        call.respondOk()
    }

    delete<Paths.BlockedWords>({
        summary = "Удалить плохие слова из приложения для режима модерации BLACKLIST."
        request {
            body<BlockedWordsRequest> {
                example("OK") {
                    value = BlockedWordsRequest(listOf("п*тон"))
                }
            }
        }

        response {
            code(HttpStatusCode.OK) {
                body<StatusResponse.Ok> {
                    example("OK") {
                        value = StatusResponse.Ok()
                    }
                }
            }
        }
    }) {
        val (words) = call.receive<BlockedWordsRequest>()
        for (word in words) {
            blockedWordRepo.delete(word)
        }

        call.respondOk()
    }

    get<Paths.BlockedWords>({
        summary = "Получить плохие слова, используемые в приложении для режима модерации BLACKLIST."
        response {
            code(HttpStatusCode.OK) {
                body<BlockedWordsRequest> {
                    example("OK") {
                        value = BlockedWordsRequest(listOf("п*тон"))
                    }
                }
            }
        }
    }) {
        call.respond(BlockedWordsRequest(blockedWordRepo.getAll()))
    }
}
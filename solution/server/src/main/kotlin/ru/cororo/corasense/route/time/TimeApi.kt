package ru.cororo.corasense.route.time

import io.github.smiley4.ktoropenapi.resources.post
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.core.component.get
import ru.cororo.corasense.inject.api
import ru.cororo.corasense.model.time.dto.TimeUpdateRequest
import ru.cororo.corasense.route.Paths
import ru.cororo.corasense.shared.service.CurrentDayService

fun Route.timeApi() = api {
    val currentDayService = it.get<CurrentDayService>()

    post<Paths.TimeAdvance>({
        summary = "Изменить время"
        description = "Машина времени (inspired by Alexander Shakhov)"

        request {
            body<TimeUpdateRequest> {
                example("Время: 1 день") {
                    value = TimeUpdateRequest(1)
                }
            }
        }

        response {
            code(HttpStatusCode.OK) {
                body<TimeUpdateRequest> {
                    example("Время: 1 день") {
                        value = TimeUpdateRequest(1)
                    }
                }
            }
        }
    }) {
        val body = call.receive<TimeUpdateRequest>()
        currentDayService.setCurrentDay(body.currentDate)
        call.respond(body)
    }
}
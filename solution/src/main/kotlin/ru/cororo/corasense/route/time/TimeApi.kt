package ru.cororo.corasense.route.time

import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import org.koin.core.component.get
import ru.cororo.corasense.inject.api
import ru.cororo.corasense.model.time.dto.TimeUpdateRequest
import ru.cororo.corasense.route.Paths
import ru.cororo.corasense.service.CurrentDayService
import ru.cororo.corasense.util.post

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
package ru.cororo.corasense.route

import io.github.smiley4.ktoropenapi.resources.get
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route

fun Route.ping() {
    get<Paths.Ping>({
        summary = "Проверка доступности приложения."

        response {
            code(HttpStatusCode.OK) {
                body<String>()
            }
        }
    }) {
        call.respondText("АЛЕКСАНДР ШАХОВ Я ВАШ ФАНАТ")
    }
}

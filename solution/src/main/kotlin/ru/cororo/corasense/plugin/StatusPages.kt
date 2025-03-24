package ru.cororo.corasense.plugin

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import ru.cororo.corasense.model.dto.Errors
import ru.cororo.corasense.model.dto.StatusCodeException
import ru.cororo.corasense.model.dto.respond

fun Application.configureStatusPages() {
    install(StatusPages) {
        status(HttpStatusCode.UnsupportedMediaType) {
            call.respond(Errors.BadRequest)
        }

        exception<StatusCodeException> { call, cause ->
            call.respond(cause.response)
        }

        exception<Throwable> { call, cause ->
            cause.printStackTrace()
            call.respond(Errors.BodyProblem(cause))
        }
    }
}
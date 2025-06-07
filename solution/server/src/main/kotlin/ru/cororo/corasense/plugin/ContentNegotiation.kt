package ru.cororo.corasense.plugin

import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import kotlinx.serialization.json.Json

fun Application.configureNegotiation() {
    install(ContentNegotiation) {
        json(Json {
            encodeDefaults = true
            explicitNulls = true // синпобеда.рф
            isLenient = false
        })
    }
}

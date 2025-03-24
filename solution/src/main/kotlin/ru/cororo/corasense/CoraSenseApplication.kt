package ru.cororo.corasense

import io.ktor.server.application.*
import io.ktor.server.netty.*
import ru.cororo.corasense.plugin.*

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
    log.info("Starting application...")

    configureDatabase()
    configureKoin()
    configureNegotiation()
    configureCORS()
    configureValidation()
    configureStatusPages()
    configureRouting()
    configureSwagger()
    configureMicrometer()
    configureTelegram()
    configureLLM()
}

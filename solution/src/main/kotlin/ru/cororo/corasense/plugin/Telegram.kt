package ru.cororo.corasense.plugin

import io.ktor.server.application.*
import org.koin.ktor.ext.get
import ru.cororo.corasense.service.TelegramBotService

fun Application.configureTelegram() {
    get<TelegramBotService>().start()

    monitor.subscribe(ApplicationStopped) {
        get<TelegramBotService>().close()
    }
}
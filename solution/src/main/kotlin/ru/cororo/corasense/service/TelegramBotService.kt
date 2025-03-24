package ru.cororo.corasense.service

import eu.vendeli.tgbot.TelegramBot
import eu.vendeli.tgbot.utils.TgException
import io.ktor.client.plugins.*
import io.ktor.server.application.*
import io.ktor.server.config.*
import kotlinx.coroutines.*
import java.io.Closeable
import kotlin.coroutines.CoroutineContext

class TelegramBotService(private val application: Application) : CoroutineScope, Closeable {
    private val telegramBotToken = application.environment.config.tryGetString("telegram.bot_token") ?: ""
    private val telegramBot = TelegramBot(telegramBotToken) {
        identifier = "CoraSense"
    }
    private val telegramEnabled =
        application.environment.config.tryGetString("telegram.enabled")?.toBooleanStrictOrNull() == true
    override val coroutineContext: CoroutineContext =
        Dispatchers.IO + CoroutineName("TelegramBotService") + SupervisorJob()

    fun start() {
        if (!telegramEnabled) return
        launch {
            try {
                telegramBot.handleUpdates()
            } catch (ex: Exception) {
                if (ex is CancellationException) return@launch
                if (ex is TgException && ex.cause is HttpRequestTimeoutException) {
                    application.log.warn("Не дождались ответа от сервера Telegram! Перезапуск бота...")
                    start()
                    return@launch
                }

                application.log.warn("Ошибка в работе telegram бота.", ex)
            }
        }
    }

    override fun close() {
        cancel()
    }
}
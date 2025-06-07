package ru.cororo.corasense.telegram

import eu.vendeli.tgbot.TelegramBot
import eu.vendeli.tgbot.utils.common.TgException
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import kotlinx.coroutines.CancellationException
import kotlinx.rpc.krpc.ktor.client.installKrpc
import kotlinx.rpc.krpc.ktor.client.rpc
import kotlinx.rpc.krpc.ktor.client.rpcConfig
import kotlinx.rpc.krpc.serialization.json.json
import kotlinx.rpc.withService
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.logger.slf4jLogger
import org.slf4j.LoggerFactory
import ru.cororo.corasense.shared.service.AdActionService
import ru.cororo.corasense.shared.service.AdvertiserService
import ru.cororo.corasense.shared.service.CampaignService
import ru.cororo.corasense.shared.service.ClientService
import ru.cororo.corasense.shared.service.CurrentDayService
import ru.cororo.corasense.shared.service.ImageService
import ru.cororo.corasense.shared.service.LlmService
import ru.cororo.corasense.shared.service.MLScoreService
import ru.cororo.corasense.shared.service.ModerationService

class TelegramBotApp(config: BotConfig) {
    private val telegramBotToken = config.botToken
    private val telegramBot = TelegramBot(telegramBotToken) {
        identifier = "CoraSense"
    }
    private val logger = LoggerFactory.getLogger(TelegramBotApp::class.java)

    suspend fun start() {
        try {
            logger.info("Starting telegram bot...")
            telegramBot.handleUpdates()
        } catch (ex: Exception) {
            if (ex is CancellationException) return
            if (ex is TgException && ex.cause is HttpRequestTimeoutException) {
                logger.warn("Не дождались ответа от сервера Telegram! Перезапуск бота...")
                start()
                return
            }

            logger.error("Ошибка в работе telegram бота.", ex)
        }
    }
}

suspend fun main() {
    val config = BotConfig(System.getenv("RPC_URL") ?: "ws://localhost:8080/rpc", System.getenv("TELEGRAM_BOT_TOKEN") ?: "")

    val httpClient = HttpClient {
        installKrpc {
            waitForServices = true
        }
    }

    val krpcClient = httpClient.rpc {
        rpcConfig {
            url(config.rpcUrl)

            serialization {
                json()
            }
        }
    }

    startKoin {
        val module = module {
            single<ClientService> { krpcClient.withService() }
            single<AdvertiserService> { krpcClient.withService() }
            single<CampaignService> { krpcClient.withService() }
            single<ImageService> { krpcClient.withService() }
            single<LlmService> { krpcClient.withService() }
            single<AdActionService> { krpcClient.withService() }
            single<ModerationService> { krpcClient.withService() }
            single<MLScoreService> { krpcClient.withService() }
            single<CurrentDayService> { krpcClient.withService() }
        }

        slf4jLogger()
        modules(module)
    }

    try {
        val telegramBotApp = TelegramBotApp(config)
        telegramBotApp.start()
    } finally {
        httpClient.close()
        stopKoin()
    }
}
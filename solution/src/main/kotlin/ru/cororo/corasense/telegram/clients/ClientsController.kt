package ru.cororo.corasense.telegram.clients

import eu.vendeli.tgbot.TelegramBot
import eu.vendeli.tgbot.annotations.CommandHandler
import eu.vendeli.tgbot.api.answer.answerCallbackQuery
import eu.vendeli.tgbot.api.message.message
import eu.vendeli.tgbot.types.User
import eu.vendeli.tgbot.types.internal.CallbackQueryUpdate
import eu.vendeli.tgbot.utils.setChain
import org.koin.core.component.get
import ru.cororo.corasense.inject.telegramApi
import ru.cororo.corasense.model.client.data.Client
import ru.cororo.corasense.service.ClientService
import java.util.*

@CommandHandler(["/clients"])
suspend fun clients(user: User, bot: TelegramBot) = telegramApi {
    message {
        "Выбери действие \uD83D\uDC47"
    }.inlineKeyboardMarkup {
        callbackData("➕ Создать нового клиента") { "create-client" }
        br()
        callbackData("\uD83D\uDD0E Найти клиента по логину") { "find-client-by-name" }
        br()
        callbackData("\uD83D\uDD0E Найти клиента по ID") { "find-client-by-id" }
    }.send(user, bot)
}

@CommandHandler.CallbackQuery(["create-client"])
suspend fun createClient(user: User, bot: TelegramBot, update: CallbackQueryUpdate) = telegramApi {
    answerCallbackQuery(update.callbackQuery.id).send(user, bot)
    message { "Для начала укажи UUID клиента, которого ты хочешь создать, либо выбери случайное значение." }
        .replyKeyboardMarkup { +"Случайное значение" }.send(user, bot)
    bot.inputListener.setChain(user, CreateClientChain.Uuid)
}

internal suspend fun sendClientInfo(user: User, bot: TelegramBot, client: Client) =
    message {
        """
        📔 ID: ${client.id}
        ✏️ Логин: ${client.login}
        🌿 Возраст: ${client.age}
        👨‍🦱 Пол: ${client.gender.russianName}
        🗺 Местоположение: ${client.location}
    """.trimIndent()
    }
        .inlineKeyboardMarkup {
            callbackData("✏\uFE0F Изменить логин") { "change-client-login?id=${client.id}" }
            br()
            callbackData("✏\uFE0F Изменить возраст") { "change-client-age?id=${client.id}" }
            br()
            callbackData("✏\uFE0F Изменить пол") { "change-client-gender?id=${client.id}" }
            br()
            callbackData("✏\uFE0F Изменить местоположение") { "change-client-location?id=${client.id}" }
            br()
            callbackData("\uD83D\uDDBC Просматривать рекламу") { "client-view-ad?id=${client.id}" }
        }
        .send(user, bot)

@CommandHandler.CallbackQuery(["configure-client"])
suspend fun configureClient(id: String, user: User, bot: TelegramBot, update: CallbackQueryUpdate) = telegramApi {
    answerCallbackQuery(update.callbackQuery.id).send(user, bot)
    val clientService = get<ClientService>()
    val client = clientService.getClient(UUID.fromString(id)) ?: run {
        message { "Клиента уже не существует! Может, вы кликнули на старую кнопку?" }.send(user, bot)
        return@telegramApi
    }

    sendClientInfo(user, bot, client)
}

package ru.cororo.corasense.telegram.clients

import eu.vendeli.tgbot.TelegramBot
import eu.vendeli.tgbot.annotations.CommandHandler
import eu.vendeli.tgbot.annotations.InputHandler
import eu.vendeli.tgbot.api.answer.answerCallbackQuery
import eu.vendeli.tgbot.api.message.message
import eu.vendeli.tgbot.types.User
import eu.vendeli.tgbot.types.internal.CallbackQueryUpdate
import eu.vendeli.tgbot.types.internal.ProcessedUpdate
import org.koin.core.component.get
import ru.cororo.corasense.inject.telegramApi
import ru.cororo.corasense.service.ClientService
import ru.cororo.corasense.telegram.cancel.cancelButton
import java.util.UUID

@CommandHandler.CallbackQuery(["find-client-by-id"])
suspend fun findClientById(user: User, bot: TelegramBot, update: CallbackQueryUpdate) = telegramApi {
    answerCallbackQuery(update.callbackQuery.id).send(user, bot)
    message { "Для выбора клиента введи его UUID \uD83D\uDDD2" }.inlineKeyboardMarkup { cancelButton() }.send(user, bot)

    bot.inputListener[user] = "client-search-id"
}

@InputHandler(["client-search-id"])
suspend fun clientSearchById(update: ProcessedUpdate, user: User, bot: TelegramBot) = telegramApi {
    val idString = update.text
    val id = try {
        UUID.fromString(idString)
    } catch (_: Exception) {
        message { "❌ Ты передал невалидный UUID! Попробуй снова." }.inlineKeyboardMarkup { cancelButton() }
            .send(user, bot)
        bot.inputListener[user] = "client-search-id"
        return@telegramApi
    }

    val clientService = get<ClientService>()
    val client = clientService.getClient(id)
    if (client == null) {
        message { "❌ Ничего не найдено. Попробуй снова." }.inlineKeyboardMarkup { cancelButton() }.send(user, bot)
        bot.inputListener[user] = "client-search-id"
        return@telegramApi
    }

    message {
        """
        🗒 Клиент найден!
        📔 ID: ${client.id}
        ✏️ Логин: ${client.login}
        🌿 Возраст: ${client.age}
        👨‍🦱 Пол: ${client.gender.russianName}
        🗺 Местоположение: ${client.location}
        """.trimIndent()
    }.inlineKeyboardMarkup {
        callbackData("\uD83D\uDD27 Настройка") { "configure-client?id=${client.id}" }
    }.send(user, bot)
}

@CommandHandler.CallbackQuery(["find-client-by-name"])
suspend fun findClientByName(user: User, bot: TelegramBot, update: CallbackQueryUpdate) = telegramApi {
    answerCallbackQuery(update.callbackQuery.id).send(user, bot)
    message { "Для выбора клиента введи его логин \uD83D\uDDD2" }.inlineKeyboardMarkup { cancelButton() }
        .send(user, bot)

    bot.inputListener[user] = "client-search-name"
}

@InputHandler(["client-search-name"])
suspend fun clientSearchByName(update: ProcessedUpdate, user: User, bot: TelegramBot) = telegramApi {
    val name = update.text
    val clientService = get<ClientService>()
    val clients = clientService.findClientsByLogin(name)
    if (clients.isEmpty()) {
        message { "❌ Ничего не найдено. Попробуй снова." }.inlineKeyboardMarkup { cancelButton() }.send(user, bot)
        bot.inputListener[user] = "client-search-name"
        return@telegramApi
    }

    message { "\uD83D\uDDD2 Вот найденные клиенты:" }.inlineKeyboardMarkup {
        clients.forEach { client ->
            callbackData("ID: ${client.id}") { "configure-client?id=${client.id}" }
        }
    }.send(user, bot)
}

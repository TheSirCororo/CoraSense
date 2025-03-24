package ru.cororo.corasense.telegram.clients

import eu.vendeli.tgbot.TelegramBot
import eu.vendeli.tgbot.annotations.CommandHandler
import eu.vendeli.tgbot.annotations.InputHandler
import eu.vendeli.tgbot.api.answer.answerCallbackQuery
import eu.vendeli.tgbot.api.message.message
import eu.vendeli.tgbot.generated.get
import eu.vendeli.tgbot.generated.set
import eu.vendeli.tgbot.types.User
import eu.vendeli.tgbot.types.internal.CallbackQueryUpdate
import eu.vendeli.tgbot.types.internal.ProcessedUpdate
import org.koin.core.component.get
import ru.cororo.corasense.inject.telegramApi
import ru.cororo.corasense.service.ClientService
import ru.cororo.corasense.telegram.cancel.cancelButton
import ru.cororo.corasense.telegram.startKeyboard
import java.util.*

@CommandHandler.CallbackQuery(["change-client-location"])
suspend fun changeClientLocation(id: String, user: User, bot: TelegramBot, update: CallbackQueryUpdate) = telegramApi {
    answerCallbackQuery(update.callbackQuery.id)
    val clientService = get<ClientService>()
    val client = clientService.getClient(UUID.fromString(id)) ?: run {
        message { "Клиента уже не существует! Может, вы кликнули на старую кнопку?" }.send(user, bot)
        return@telegramApi
    }

    message { "Введи местоположение клиента." }.inlineKeyboardMarkup {
        cancelButton()
    }.send(user, bot)

    user["editing-client-id"] = client.id.toString()
    bot.inputListener[user] = "client-location"
}

@InputHandler(["client-location"])
suspend fun clientLocationInput(update: ProcessedUpdate, user: User, bot: TelegramBot) = telegramApi {
    val id = user["editing-client-id"]
    val clientService = get<ClientService>()
    val client = clientService.getClient(UUID.fromString(id)) ?: run {
        message { "Клиента уже не существует! Может, вы кликнули на старую кнопку?" }.send(user, bot)
        return@telegramApi
    }

    val newLocation = update.text
    if (newLocation.length !in 3..128) {
        message { "Введи строку с местоположением от 3 до 128 символов." }.inlineKeyboardMarkup { cancelButton() }
            .send(user, bot)
        bot.inputListener[user] = "client-location"
        return@telegramApi
    }

    val newClient = client.copy(location = newLocation)
    clientService.saveClient(newClient)
    message { "✅ Местоположение клиента изменено на $newLocation!" }.startKeyboard().send(user, bot)
    sendClientInfo(user, bot, newClient)
}

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

@CommandHandler.CallbackQuery(["change-client-login"])
suspend fun changeClientLogin(id: String, user: User, bot: TelegramBot, update: CallbackQueryUpdate) = telegramApi {
    answerCallbackQuery(update.callbackQuery.id)
    val clientService = get<ClientService>()
    val client = clientService.getClient(UUID.fromString(id)) ?: run {
        message { "Клиента уже не существует! Может, вы кликнули на старую кнопку?" }.send(user, bot)
        return@telegramApi
    }

    message { "Введи новый логин для клиента." }.inlineKeyboardMarkup {
        cancelButton()
    }.send(user, bot)

    user["editing-client-id"] = client.id.toString()
    bot.inputListener[user] = "client-login"
}

@InputHandler(["client-login"])
suspend fun clientLoginInput(update: ProcessedUpdate, user: User, bot: TelegramBot) = telegramApi {
    val id = user["editing-client-id"]
    val clientService = get<ClientService>()
    val client = clientService.getClient(UUID.fromString(id)) ?: run {
        message { "Клиента уже не существует! Может, вы кликнули на старую кнопку?" }.send(user, bot)
        return@telegramApi
    }

    val newLogin = update.text
    if (newLogin.length !in 3..128) {
        message { "Логин клиента должен быть длиной от 3 до 128 символов. Попробуйте снова." }.send(user, bot)
        bot.inputListener[user] = "client-login"
        return@telegramApi
    }

    val newClient = client.copy(login = newLogin)
    clientService.saveClient(newClient)
    message { "✅ Логин клиента изменён на $newLogin!" }.startKeyboard().send(user, bot)
    sendClientInfo(user, bot, newClient)
}

package ru.cororo.corasense.telegram

import eu.vendeli.tgbot.TelegramBot
import eu.vendeli.tgbot.annotations.CommandHandler
import eu.vendeli.tgbot.api.botactions.setMyCommands
import eu.vendeli.tgbot.api.message.message
import eu.vendeli.tgbot.interfaces.action.TgAction
import eu.vendeli.tgbot.interfaces.features.MarkupFeature
import eu.vendeli.tgbot.types.User
import ru.cororo.corasense.inject.telegramApi

@CommandHandler(["/start"])
suspend fun start(user: User, bot: TelegramBot) = telegramApi {
    setMyCommands {
        botCommand("/advertisers", "Управление рекламодателями")
        botCommand("/clients", "Управление клиентами")
        botCommand("/images", "Управление изображениями")
        botCommand("/time", "Управление временем")
    }.send(bot)

    message {
        "Привет! Используй кнопки ниже для работы с ботом."
    }.startKeyboard().send(user, bot)
}

fun <Return : TgAction<*>> MarkupFeature<Return>.startKeyboard() = replyKeyboardMarkup {
    +"/advertisers"
    +"/clients"
    br()
    +"/images"
    +"/time"
}

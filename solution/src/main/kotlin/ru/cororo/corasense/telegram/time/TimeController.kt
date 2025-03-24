package ru.cororo.corasense.telegram.time

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
import ru.cororo.corasense.service.CurrentDayService
import ru.cororo.corasense.telegram.cancel.cancelButton

@CommandHandler(["/time"])
suspend fun timeCommand(user: User, bot: TelegramBot) = telegramApi {
    val currentDayService = get<CurrentDayService>()
    val currentDay = currentDayService.getCurrentDay()
    message { "\uD83D\uDD53 Управление временем.\nТекущий день: $currentDay" }
        .inlineKeyboardMarkup {
            callbackData("Изменить день") { "change-day" }
        }.send(user, bot)
}

@CommandHandler.CallbackQuery(["change-day"])
suspend fun changeDay(user: User, bot: TelegramBot, update: CallbackQueryUpdate) = telegramApi {
    answerCallbackQuery(update.callbackQuery.id).send(user, bot)
    message { "Введи новый день." }.inlineKeyboardMarkup { cancelButton() }.send(user, bot)
    bot.inputListener[user] = "change-day"
}

@InputHandler(["change-day"])
suspend fun changeDayInput(user: User, bot: TelegramBot, update: ProcessedUpdate) = telegramApi {
    val day = update.text.toIntOrNull()
    if (day == null || day < 0) {
        message { "День должен быть целым неотрицательным числом. Повтори попытку." }
            .inlineKeyboardMarkup { cancelButton() }.send(user, bot)
        bot.inputListener[user] = "change-day"
        return@telegramApi
    }

    val currentDayService = get<CurrentDayService>()
    currentDayService.setCurrentDay(day)
    message { "Новый день: $day." }.send(user, bot)
}

package ru.cororo.corasense.telegram.handler.cancel

import eu.vendeli.tgbot.TelegramBot
import eu.vendeli.tgbot.annotations.CommandHandler
import eu.vendeli.tgbot.api.answer.answerCallbackQuery
import eu.vendeli.tgbot.api.message.message
import eu.vendeli.tgbot.types.User
import eu.vendeli.tgbot.types.component.CallbackQueryUpdate
import eu.vendeli.tgbot.utils.builders.InlineKeyboardMarkupBuilder
import ru.cororo.corasense.telegram.handler.startKeyboard

@CommandHandler.CallbackQuery(["cancel"])
suspend fun cancel(user: User, bot: TelegramBot, update: CallbackQueryUpdate) {
    answerCallbackQuery(update.callbackQuery.id).send(user, bot)
    bot.inputListener.del(user.id)
    message { "❌ Ввод отменён." }.startKeyboard().send(user, bot)
}

fun InlineKeyboardMarkupBuilder.cancelButton() = callbackData("❌ Отменить") { "cancel" }
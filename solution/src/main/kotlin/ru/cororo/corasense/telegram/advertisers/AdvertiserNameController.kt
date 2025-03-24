package ru.cororo.corasense.telegram.advertisers

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
import ru.cororo.corasense.service.AdvertiserService
import ru.cororo.corasense.telegram.cancel.cancelButton
import ru.cororo.corasense.telegram.startKeyboard
import java.util.*


@CommandHandler.CallbackQuery(["change-advertiser-name"])
suspend fun changeAdvertiserName(id: String, user: User, bot: TelegramBot, update: CallbackQueryUpdate) = telegramApi {
    answerCallbackQuery(update.callbackQuery.id)
    val advertiserService = get<AdvertiserService>()
    val advertiser = advertiserService.getAdvertiser(UUID.fromString(id)) ?: run {
        message { "Рекламодателя уже не существует! Может, вы кликнули на старую кнопку?" }.send(user, bot)
        return@telegramApi
    }

    message { "Введи новое имя для рекламодателя" }.inlineKeyboardMarkup {
        cancelButton()
    }.send(user, bot)

    user["editing-advertiser-id"] = advertiser.id.toString()
    bot.inputListener[user] = "advertiser-name"
}

@InputHandler(["advertiser-name"])
suspend fun advertiserNameInput(update: ProcessedUpdate, user: User, bot: TelegramBot) = telegramApi {
    val id = user["editing-advertiser-id"]
    val advertiserService = get<AdvertiserService>()
    val advertiser = advertiserService.getAdvertiser(UUID.fromString(id)) ?: run {
        message { "Рекламодателя уже не существует! Может, вы кликнули на старую кнопку?" }.send(user, bot)
        return@telegramApi
    }

    val newName = update.text
    if (newName.length !in 3..128) {
        message { "Имя рекламодателя должно быть длиной от 3 до 128 символов. Попробуйте снова." }
            .inlineKeyboardMarkup { cancelButton() }
            .send(user, bot)
        bot.inputListener[user] = "advertiser-name"
        return@telegramApi
    }

    advertiserService.saveAdvertiser(advertiser.copy(name = newName))
    message { "✅ Имя рекламодателя изменено на $newName!" }.startKeyboard().send(user, bot)
}

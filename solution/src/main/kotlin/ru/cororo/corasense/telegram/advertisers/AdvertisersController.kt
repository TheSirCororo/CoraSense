package ru.cororo.corasense.telegram.advertisers

import eu.vendeli.tgbot.TelegramBot
import eu.vendeli.tgbot.annotations.CommandHandler
import eu.vendeli.tgbot.api.answer.answerCallbackQuery
import eu.vendeli.tgbot.api.message.message
import eu.vendeli.tgbot.types.User
import eu.vendeli.tgbot.types.internal.CallbackQueryUpdate
import eu.vendeli.tgbot.utils.setChain
import org.koin.core.component.get
import ru.cororo.corasense.inject.telegramApi
import ru.cororo.corasense.service.AdvertiserService
import java.util.*

@CommandHandler(["/advertisers"])
suspend fun advertisers(user: User, bot: TelegramBot) = telegramApi {
    message {
        "Выбери действие \uD83D\uDC47"
    }.inlineKeyboardMarkup {
        callbackData("➕ Создать нового рекламодателя") { "create-advertiser" }
        br()
        callbackData("\uD83D\uDD0E Найти рекламодателя по имени") { "find-advertiser-by-name" }
        br()
        callbackData("\uD83D\uDD0E Найти рекламодателя по ID") { "find-advertiser-by-id" }
        br()
        callbackData("\uD83D\uDD0E Управление рекламными кампаниями") { "advertiser-select-manage-campaigns" }
    }.send(user, bot)
}

@CommandHandler.CallbackQuery(["create-advertiser"])
suspend fun createAdvertiser(user: User, bot: TelegramBot, update: CallbackQueryUpdate) = telegramApi {
    answerCallbackQuery(update.callbackQuery.id).send(user, bot)
    message { "Для начала укажи UUID рекламодателя, которого ты хочешь создать, либо выбери случайное значение." }
        .replyKeyboardMarkup { +"Случайное значение" }.send(user, bot)
    bot.inputListener.setChain(user, CreateAdvertiserChain.Uuid)
}

@CommandHandler.CallbackQuery(["configure-advertiser"])
suspend fun configureAdvertiser(id: String, user: User, bot: TelegramBot, update: CallbackQueryUpdate) = telegramApi {
    answerCallbackQuery(update.callbackQuery.id).send(user, bot)
    val advertiserService = get<AdvertiserService>()
    val advertiser = advertiserService.getAdvertiser(UUID.fromString(id)) ?: run {
        message { "Рекламодателя уже не существует! Может, вы кликнули на старую кнопку?" }.send(user, bot)
        return@telegramApi
    }

    message { "\uD83D\uDDD2 Рекламодатель ${advertiser.name}\nID: ${advertiser.id}" }
        .inlineKeyboardMarkup {
            callbackData("✏\uFE0F Изменить имя") { "change-advertiser-name?id=$id" }
            br()
            callbackData("\uD83D\uDD27 Управление рекламными кампаниями") { "mc?id=$id&page=1&new=true" }
        }
        .send(user, bot)
}

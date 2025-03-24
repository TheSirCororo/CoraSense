package ru.cororo.corasense.telegram.advertisers

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
import ru.cororo.corasense.service.AdvertiserService
import ru.cororo.corasense.telegram.cancel.cancelButton
import java.util.*


@CommandHandler.CallbackQuery(["find-advertiser-by-id"])
suspend fun findAdvertiserById(user: User, bot: TelegramBot, update: CallbackQueryUpdate) = telegramApi {
    answerCallbackQuery(update.callbackQuery.id).send(user, bot)
    message { "Для выбора рекламодателя введи его UUID \uD83D\uDDD2" }.inlineKeyboardMarkup { cancelButton() }
        .send(user, bot)

    bot.inputListener[user] = "advertiser-search-id"
}

@InputHandler(["advertiser-search-id"])
suspend fun advertiserSearchById(update: ProcessedUpdate, user: User, bot: TelegramBot) = telegramApi {
    val idString = update.text
    val id = try {
        UUID.fromString(idString)
    } catch (_: Exception) {
        message { "❌ Ты передал невалидный UUID! Попробуй снова." }.inlineKeyboardMarkup { cancelButton() }
            .send(user, bot)
        bot.inputListener[user] = "advertiser-search-id"
        return@telegramApi
    }

    val advertiserService = get<AdvertiserService>()
    val advertiser = advertiserService.getAdvertiser(id)
    if (advertiser == null) {
        message { "❌ Ничего не найдено. Попробуй снова." }.inlineKeyboardMarkup { cancelButton() }.send(user, bot)
        bot.inputListener[user] = "advertiser-search-id"
        return@telegramApi
    }

    message { "Рекламодатель найден!\nИмя: ${advertiser.name}\nID: ${advertiser.id}" }
        .inlineKeyboardMarkup {
            callbackData("\uD83D\uDD27 Настройка") { "configure-advertiser?id=${advertiser.id}" }
        }.send(user, bot)
}

@CommandHandler.CallbackQuery(["find-advertiser-by-name"])
suspend fun findAdvertiserByName(user: User, bot: TelegramBot, update: CallbackQueryUpdate) = telegramApi {
    answerCallbackQuery(update.callbackQuery.id).send(user, bot)
    message { "Для выбора рекламодателя введи его имя \uD83D\uDDD2" }.inlineKeyboardMarkup { cancelButton() }
        .send(user, bot)

    bot.inputListener[user] = "advertiser-search-name"
}

@InputHandler(["advertiser-search-name"])
suspend fun advertiserSearchByName(update: ProcessedUpdate, user: User, bot: TelegramBot) = telegramApi {
    val name = update.text
    val advertiserService = get<AdvertiserService>()
    val advertisers = advertiserService.findAdvertisersByName(name)
    if (advertisers.isEmpty()) {
        message { "❌ Ничего не найдено. Попробуй снова." }.inlineKeyboardMarkup { cancelButton() }.send(user, bot)
        bot.inputListener[user] = "advertiser-search-name"
        return@telegramApi
    }

    message { "\uD83D\uDDD2 Вот найденные рекламодатели:" }
        .inlineKeyboardMarkup {
            advertisers.forEach { advertiser ->
                callbackData("ID: ${advertiser.id}") { "configure-advertiser?id=${advertiser.id}" }
            }
        }.send(user, bot)
}

@CommandHandler.CallbackQuery(["advertiser-select-manage-campaigns"])
suspend fun selectAdvertiserManageCampaigns(user: User, bot: TelegramBot, update: CallbackQueryUpdate) = telegramApi {
    answerCallbackQuery(update.callbackQuery.id).send(user, bot)
    message { "Для управления кампаниями необходимо выбрать рекламодателя. Введи его ID \uD83D\uDDD2" }
        .inlineKeyboardMarkup { cancelButton() }
        .send(user, bot)

    bot.inputListener[user] = "advertiser-search-id-campaigns"
}

@InputHandler(["advertiser-search-id-campaigns"])
suspend fun advertiserSelectCampaigns(update: ProcessedUpdate, user: User, bot: TelegramBot) = telegramApi {
    val idString = update.text
    val id = try {
        UUID.fromString(idString)
    } catch (_: Exception) {
        message { "❌ Ты передал невалидный UUID! Попробуй снова." }.inlineKeyboardMarkup { cancelButton() }
            .send(user, bot)
        bot.inputListener[user] = "advertiser-search-id-campaigns"
        return@telegramApi
    }

    val advertiserService = get<AdvertiserService>()
    val advertiser = advertiserService.getAdvertiser(id)
    if (advertiser == null) {
        message { "❌ Ничего не найдено. Попробуй снова." }.inlineKeyboardMarkup { cancelButton() }.send(user, bot)
        bot.inputListener[user] = "advertiser-search-id-campaigns"
        return@telegramApi
    }

    message { "Рекламодатель найден!\nИмя: ${advertiser.name}\nID: ${advertiser.id}" }
        .inlineKeyboardMarkup {
            callbackData("\uD83D\uDD27 Управление рекламными кампаниями") { "mc?id=${advertiser.id}&page=1&new=true" }
        }.send(user, bot)
}
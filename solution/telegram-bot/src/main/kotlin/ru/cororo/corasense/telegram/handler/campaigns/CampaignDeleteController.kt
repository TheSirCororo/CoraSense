package ru.cororo.corasense.telegram.handler.campaigns

import eu.vendeli.tgbot.TelegramBot
import eu.vendeli.tgbot.annotations.CommandHandler
import eu.vendeli.tgbot.api.answer.answerCallbackQuery
import eu.vendeli.tgbot.api.message.editMessageText
import eu.vendeli.tgbot.api.message.message
import eu.vendeli.tgbot.types.User
import eu.vendeli.tgbot.types.component.CallbackQueryUpdate
import org.koin.core.component.get
import ru.cororo.corasense.shared.service.CampaignService
import ru.cororo.corasense.telegram.util.telegramApi
import java.util.*

@CommandHandler.CallbackQuery(["c-delete"])
suspend fun deleteCampaign(id: String, user: User, bot: TelegramBot, update: CallbackQueryUpdate) = telegramApi {
    answerCallbackQuery(update.callbackQuery.id).send(user, bot)

    val id = UUID.fromString(id)
    val campaignService = get<CampaignService>()
    campaignService.getCampaign(id) ?: run {
        message { "Этой кампании уже не существует! Может, ты кликнул на старую кнопку?" }.send(user, bot)
        return@telegramApi
    }

    message { "Ты уверен, что хочешь удалить рекламную кампанию?" }
        .inlineKeyboardMarkup {
            callbackData("✅ Согласен") { "c-delete-yes?id=$id" }
            callbackData("❌ Не согласен") { "c-delete-no" }
        }.send(user, bot)
}

@CommandHandler.CallbackQuery(["c-delete-yes"])
suspend fun deleteCampaignYes(id: String, user: User, bot: TelegramBot, update: CallbackQueryUpdate) = telegramApi {
    answerCallbackQuery(update.callbackQuery.id).send(user, bot)

    val id = UUID.fromString(id)
    val campaignService = get<CampaignService>()
    val campaign = campaignService.getCampaign(id) ?: run {
        message { "Этой кампании уже не существует! Может, ты кликнул на старую кнопку?" }.send(user, bot)
        return@telegramApi
    }

    campaignService.deleteCampaign(campaign.id)
    editMessageText(update.callbackQuery.message!!.messageId) {
        "✅ Кампания удалена."
    }.send(user, bot)
}

@CommandHandler.CallbackQuery(["c-delete-no"])
suspend fun deleteCampaignNo(user: User, bot: TelegramBot, update: CallbackQueryUpdate) = telegramApi {
    answerCallbackQuery(update.callbackQuery.id).send(user, bot)
    editMessageText(update.callbackQuery.message!!.messageId) {
        "❌ Удаление кампании отменено."
    }.send(user, bot)
}

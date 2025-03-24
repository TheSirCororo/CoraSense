package ru.cororo.corasense.telegram.campaigns

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
import ru.cororo.corasense.service.CampaignService
import ru.cororo.corasense.telegram.cancel.cancelButton
import java.util.*

@CommandHandler.CallbackQuery(["c-cl-cost"])
suspend fun changeClickCost(id: String, user: User, bot: TelegramBot, update: CallbackQueryUpdate) =
    changeCost(id, user, bot, update)

@CommandHandler.CallbackQuery(["c-im-cost"])
suspend fun changeImCost(id: String, user: User, bot: TelegramBot, update: CallbackQueryUpdate) =
    changeCost(id, user, bot, update)

suspend fun changeCost(id: String, user: User, bot: TelegramBot, update: CallbackQueryUpdate) = telegramApi {
    answerCallbackQuery(update.callbackQuery.id).send(user, bot)

    val id = UUID.fromString(id)
    val campaignService = get<CampaignService>()
    val campaign = campaignService.getCampaign(id) ?: run {
        message { "Этой кампании уже не существует! Может, ты кликнул на старую кнопку?" }.send(user, bot)
        return@telegramApi
    }

    val changingImpressionsCost = update.callbackQuery.data!!.contains("-im-")
    message { "Введите новую цену ${if (changingImpressionsCost) "показа" else "клика"} для кампании." }.inlineKeyboardMarkup { cancelButton() }
        .send(user, bot)
    user["changing-campaign-id"] = campaign.id.toString()
    user["changing-impressions-cost"] = changingImpressionsCost.toString()
    bot.inputListener[user] = "c-change-cost"
}

@InputHandler(["c-change-cost"])
suspend fun changeCostInput(user: User, bot: TelegramBot, update: ProcessedUpdate) = telegramApi {
    val id = UUID.fromString(user["changing-campaign-id"])
    val campaignService = get<CampaignService>()
    val campaign = campaignService.getCampaign(id) ?: run {
        message { "Этой кампании уже не существует! Может, ты кликнул на старую кнопку?" }.send(user, bot)
        return@telegramApi
    }

    val changingImpressionsCost = user["changing-impressions-cost"]?.toBooleanStrictOrNull() ?: return@telegramApi
    val cost = update.text.toDoubleOrNull()
    if (cost == null || cost < 0) {
        message { "Цена должна быть неотрицательным вещественным числом." }.inlineKeyboardMarkup { cancelButton() }
            .send(user, bot)
        bot.inputListener[user] = "c-change-cost"
        return@telegramApi
    }

    val newCampaign =
        if (changingImpressionsCost) campaign.copy(costPerImpression = cost) else campaign.copy(costPerClick = cost)
    campaignService.saveCampaign(newCampaign)
    message { "Цена успешно изменена!" }.inlineKeyboardMarkup {
        callbackData("Вернуться к редактированию кампании") { "edit-campaign?id=$id" }
    }.send(user, bot)
}

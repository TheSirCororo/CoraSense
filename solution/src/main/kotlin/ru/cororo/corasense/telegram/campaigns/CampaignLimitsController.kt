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
import ru.cororo.corasense.model.campaign.data.hasStarted
import ru.cororo.corasense.service.CampaignService
import ru.cororo.corasense.service.CurrentDayService
import ru.cororo.corasense.telegram.cancel.cancelButton
import java.util.*

@CommandHandler.CallbackQuery(["c-im-limit"])
suspend fun changeImLimit(id: String, user: User, bot: TelegramBot, update: CallbackQueryUpdate) =
    changeLimit(id, user, bot, update)

@CommandHandler.CallbackQuery(["c-cl-limit"])
suspend fun changeClickLimit(id: String, user: User, bot: TelegramBot, update: CallbackQueryUpdate) =
    changeLimit(id, user, bot, update)

suspend fun changeLimit(id: String, user: User, bot: TelegramBot, update: CallbackQueryUpdate) = telegramApi {
    answerCallbackQuery(update.callbackQuery.id).send(user, bot)

    val id = UUID.fromString(id)
    val campaignService = get<CampaignService>()
    val campaign = campaignService.getCampaign(id) ?: run {
        message { "Этой кампании уже не существует! Может, ты кликнул на старую кнопку?" }.send(user, bot)
        return@telegramApi
    }

    val currentDayService = get<CurrentDayService>()
    if (campaign.hasStarted(currentDayService)) {
        message { "Нельзя изменить лимиты или даты после начала кампании." }.send(user, bot)
        return@telegramApi
    }

    message { "Введите новый лимит для кампании." }.inlineKeyboardMarkup { cancelButton() }.send(user, bot)
    user["changing-campaign-id"] = campaign.id.toString()
    user["changing-clicks-limit"] = if (update.callbackQuery.data!!.contains("-cl-")) {
        "true"
    } else "false"
    bot.inputListener[user] = "c-change-limit"
}

@InputHandler(["c-change-limit"])
suspend fun changeLimitInput(user: User, bot: TelegramBot, update: ProcessedUpdate) = telegramApi {
    val id = UUID.fromString(user["changing-campaign-id"])
    val campaignService = get<CampaignService>()
    val campaign = campaignService.getCampaign(id) ?: run {
        message { "Этой кампании уже не существует! Может, ты кликнул на старую кнопку?" }.send(user, bot)
        return@telegramApi
    }

    val changingClicksLimit = user["changing-clicks-limit"]?.toBooleanStrictOrNull() ?: return@telegramApi
    val limit = update.text.toIntOrNull()
    if (limit == null || limit < 0) {
        message { "Лимит должен быть целым неотрицательным числом." }.inlineKeyboardMarkup { cancelButton() }
            .send(user, bot)
        bot.inputListener[user] = "c-change-limit"
        return@telegramApi
    }

    val newCampaign =
        if (changingClicksLimit) campaign.copy(clicksLimit = limit) else campaign.copy(impressionsLimit = limit)
    campaignService.saveCampaign(newCampaign)
    message { "Лимит успешно изменён!" }.inlineKeyboardMarkup {
        callbackData("Вернуться к редактированию кампании") { "edit-campaign?id=$id" }
    }.send(user, bot)
}

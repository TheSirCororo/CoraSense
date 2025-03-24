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
import java.util.UUID
import kotlin.text.toBooleanStrictOrNull

@CommandHandler.CallbackQuery(["c-st-date"])
suspend fun changeStartDate(id: String, user: User, bot: TelegramBot, update: CallbackQueryUpdate) =
    changeDate(id, user, bot, update)

@CommandHandler.CallbackQuery(["c-end-date"])
suspend fun changeEndDate(id: String, user: User, bot: TelegramBot, update: CallbackQueryUpdate) =
    changeDate(id, user, bot, update)

suspend fun changeDate(id: String, user: User, bot: TelegramBot, update: CallbackQueryUpdate) = telegramApi {
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

    val changingStartDate = update.callbackQuery.data!!.contains("-st-")
    message { "Введите новую дату ${if (changingStartDate) "начала" else "конца"} показа для кампании." }.inlineKeyboardMarkup { cancelButton() }.send(user, bot)
    user["changing-campaign-id"] = campaign.id.toString()
    user["changing-start-date"] = changingStartDate.toString()
    bot.inputListener[user] = "c-change-date"
}

@InputHandler(["c-change-date"])
suspend fun changeDateInput(user: User, bot: TelegramBot, update: ProcessedUpdate) = telegramApi {
    val id = UUID.fromString(user["changing-campaign-id"])
    val campaignService = get<CampaignService>()
    val campaign = campaignService.getCampaign(id) ?: run {
        message { "Этой кампании уже не существует! Может, ты кликнул на старую кнопку?" }.send(user, bot)
        return@telegramApi
    }

    val changingStartDate = user["changing-start-date"]?.toBooleanStrictOrNull() ?: return@telegramApi
    val date = update.text.toIntOrNull()
    if (date == null || date < 0) {
        message { "Дата должна быть целым неотрицательным числом." }.inlineKeyboardMarkup { cancelButton() }
            .send(user, bot)
        bot.inputListener[user] = "c-change-date"
        return@telegramApi
    }

    val newCampaign =
        if (changingStartDate) campaign.copy(startDate = date) else campaign.copy(endDate = date)
    campaignService.saveCampaign(newCampaign)
    message { "Дата успешно изменена!" }.inlineKeyboardMarkup {
        callbackData("Вернуться к редактированию кампании") { "edit-campaign?id=$id" }
    }.send(user, bot)
}

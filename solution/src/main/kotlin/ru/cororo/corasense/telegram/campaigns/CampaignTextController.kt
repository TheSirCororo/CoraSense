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
import ru.cororo.corasense.model.moderation.data.ModerationScope
import ru.cororo.corasense.service.CampaignService
import ru.cororo.corasense.service.ModerationService
import ru.cororo.corasense.telegram.cancel.cancelButton
import java.util.UUID

@CommandHandler.CallbackQuery(["c-text"])
suspend fun changeText(id: String, user: User, bot: TelegramBot, update: CallbackQueryUpdate) = telegramApi {
    answerCallbackQuery(update.callbackQuery.id).send(user, bot)

    val id = UUID.fromString(id)
    val campaignService = get<CampaignService>()
    val campaign = campaignService.getCampaign(id) ?: run {
        message { "Этой кампании уже не существует! Может, ты кликнул на старую кнопку?" }.send(user, bot)
        return@telegramApi
    }

    message { "Введите новый заголовок для кампании." }.inlineKeyboardMarkup { cancelButton() }.send(user, bot)
    user["changing-campaign-id"] = campaign.id.toString()
    bot.inputListener[user] = "c-change-text"
}

@InputHandler(["c-change-text"])
suspend fun changeTextInput(user: User, bot: TelegramBot, update: ProcessedUpdate) = telegramApi {
    val id = UUID.fromString(user["changing-campaign-id"])
    val campaignService = get<CampaignService>()
    val campaign = campaignService.getCampaign(id) ?: run {
        message { "Этой кампании уже не существует! Может, ты кликнул на старую кнопку?" }.send(user, bot)
        return@telegramApi
    }

    if (update.text.isBlank() || update.text.isEmpty()) {
        message { "Текст рекламы не может быть пустым!" }.inlineKeyboardMarkup { cancelButton() }
            .send(user, bot)
        bot.inputListener[user] = "c-change-text"
        return@telegramApi
    }

    val moderationService = get<ModerationService>()
    val verdict = moderationService.moderateIfNeed(ModerationScope.AD_TEXT, update.text)
    if (!verdict.allowed) {
        message { "Текст не прошёл модерацию. Вердикт: ${verdict.reason}" }.inlineKeyboardMarkup { cancelButton() }.send(user, bot)
        bot.inputListener[user] = "c-change-text"
    }

    val newCampaign = campaign.copy(adText = update.text)
    campaignService.saveCampaign(newCampaign)
    message { "Текст рекламы успешно изменён!" }.inlineKeyboardMarkup {
        callbackData("Вернуться к редактированию кампании") { "edit-campaign?id=$id" }
    }.send(user, bot)
}

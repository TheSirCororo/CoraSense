package ru.cororo.corasense.telegram.handler.campaigns

import eu.vendeli.tgbot.TelegramBot
import eu.vendeli.tgbot.annotations.CommandHandler
import eu.vendeli.tgbot.api.answer.answerCallbackQuery
import eu.vendeli.tgbot.api.message.message
import eu.vendeli.tgbot.generated.set
import eu.vendeli.tgbot.types.User
import eu.vendeli.tgbot.types.component.CallbackQueryUpdate
import eu.vendeli.tgbot.utils.common.setChain
import org.koin.core.component.get
import ru.cororo.corasense.shared.service.CampaignService
import ru.cororo.corasense.telegram.util.telegramApi
import java.util.*

@CommandHandler.CallbackQuery(["c-targeting"])
suspend fun changeTargeting(id: String, user: User, bot: TelegramBot, update: CallbackQueryUpdate) = telegramApi {
    answerCallbackQuery(update.callbackQuery.id).send(user, bot)

    val id = UUID.fromString(id)
    val campaignService = get<CampaignService>()
    val campaign = campaignService.getCampaign(id) ?: run {
        message { "Этой кампании уже не существует! Может, ты кликнул на старую кнопку?" }.send(user, bot)
        return@telegramApi
    }

    user["editing-campaign-id"] = campaign.id.toString()
    message {
        "Выбери пол клиентов, которым хочешь показывать рекламу (MALE, FEMALE, ALL)."
    }.replyKeyboardMarkup {
        +"MALE"
        +"FEMALE"
        +"ALL"
    }.send(user, bot)

    bot.inputListener.setChain(user, CampaignTargetingChain.Gender)
}
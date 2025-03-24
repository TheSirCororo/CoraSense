package ru.cororo.corasense.telegram.campaigns

import eu.vendeli.tgbot.TelegramBot
import eu.vendeli.tgbot.annotations.CommandHandler
import eu.vendeli.tgbot.api.answer.answerCallbackQuery
import eu.vendeli.tgbot.api.message.message
import eu.vendeli.tgbot.types.User
import eu.vendeli.tgbot.types.internal.CallbackQueryUpdate
import org.koin.core.component.get
import ru.cororo.corasense.inject.telegramApi
import ru.cororo.corasense.service.AdvertiserService
import ru.cororo.corasense.service.CampaignService
import ru.cororo.corasense.service.LlmService
import java.util.*

private val latestUsedMap = mutableMapOf<UUID, Long>()

@CommandHandler.CallbackQuery(["c-ai-text"])
suspend fun campaignAiText(id: String, user: User, bot: TelegramBot, update: CallbackQueryUpdate) = telegramApi {
    answerCallbackQuery(update.callbackQuery.id).send(user, bot)

    val id = UUID.fromString(id)
    val currentTime = System.currentTimeMillis()
    val lastUsed = latestUsedMap[id]
    if (lastUsed != null && currentTime - lastUsed < 5000) {
        val remain = (lastUsed + 5000L - currentTime) / 1000L
        message { "Нельзя так часто делать запросы к ИИ! Подожди ещё $remain секунды." }.inlineKeyboardMarkup {
            callbackData("\uD83D\uDD04 Повторить попытку") { "c-ai-text?id=$id" }
        }.send(user, bot)
        return@telegramApi
    }

    latestUsedMap[id] = currentTime
    val campaignService = get<CampaignService>()
    val campaign = campaignService.getCampaign(id) ?: run {
        message { "Этой кампании уже не существует! Может, ты кликнул на старую кнопку?" }.send(user, bot)
        return@telegramApi
    }

    val adTitle = campaign.adTitle
    message { "\uD83E\uDE84 Генерируем текст при помощи ИИ...\nИспользуемый заголовок рекламы: $adTitle" }.send(
        user,
        bot
    )
    val llmService = get<LlmService>()
    val advertiserService = get<AdvertiserService>()
    val advertiser = advertiserService.getAdvertiser(campaign.advertiserId) ?: return@telegramApi
    val aiText = llmService.generateCampaignText(campaign.adTitle, advertiser.name)
    message { "✨ Сгенерированный текст: $aiText" }.inlineKeyboardMarkup {
        callbackData("\uD83D\uDD04 Перегенерировать текст") { "c-ai-text?id=$id" }
    }.send(user, bot)
}

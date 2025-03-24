package ru.cororo.corasense.telegram.campaigns

import eu.vendeli.tgbot.TelegramBot
import eu.vendeli.tgbot.annotations.CommandHandler
import eu.vendeli.tgbot.api.answer.answerCallbackQuery
import eu.vendeli.tgbot.api.message.editMessageText
import eu.vendeli.tgbot.api.message.message
import eu.vendeli.tgbot.generated.set
import eu.vendeli.tgbot.interfaces.action.Action
import eu.vendeli.tgbot.types.User
import eu.vendeli.tgbot.types.internal.CallbackQueryUpdate
import eu.vendeli.tgbot.utils.setChain
import org.koin.core.component.get
import ru.cororo.corasense.inject.telegramApi
import ru.cororo.corasense.model.campaign.data.Campaign
import ru.cororo.corasense.service.AdvertiserService
import ru.cororo.corasense.service.CampaignService
import ru.cororo.corasense.telegram.cancel.cancelButton
import java.util.*
import kotlin.math.ceil

private const val pageSize = 5

@CommandHandler.CallbackQuery(["mc"])
suspend fun manageCampaigns(
    id: String, page: String, new: String, user: User, bot: TelegramBot, update: CallbackQueryUpdate
) = telegramApi {
    answerCallbackQuery(update.callbackQuery.id).send(user, bot)

    val id = UUID.fromString(id)
    val page = page.toInt()
    val new = new.toBooleanStrictOrNull() ?: return@telegramApi
    val advertiserService = get<AdvertiserService>()
    val fromMessageId = update.callbackQuery.message?.messageId
    if (fromMessageId == null && !new) return@telegramApi

    advertiserService.getAdvertiser(id) ?: run {
        message { "Рекламодателя уже не существует! Может, вы кликнули на старую кнопку?" }.send(user, bot)
        return@telegramApi
    }

    val campaignService = get<CampaignService>()
    val (campaigns, totalCount) = campaignService.getAdvertiserCampaigns(id, (page - 1).toLong() * pageSize, pageSize)
    if (campaigns.isEmpty() && !new) return@telegramApi

    val pageCount = ceil(totalCount.toDouble() / pageSize).toInt()
    val action = (if (new) {
        message { "Доступные кампании:" }
    } else editMessageText(fromMessageId!!) { "Доступные кампании:" }).inlineKeyboardMarkup {
        campaigns.forEach {
            val shortId = it.id.toString().let { it.substring(0, 9) + "-...-" + it.substring(32, 36) }
            callbackData("${it.adTitle} (ID: $shortId)") { "edit-campaign?id=${it.id}" }
            br()
        }

        callbackData("◀\uFE0F") { "mc?id=$id&page=${page - 1}&new=false" }
        callbackData("$page / $pageCount") { "nothing" }
        callbackData("▶\uFE0F") { "mc?id=$id&page=${page + 1}&new=false" }
        br()
        callbackData("➕ Создать новую кампанию") { "new-campaign?id=$id" }
    } as Action<*>

    action.send(user, bot)
}

@CommandHandler.CallbackQuery(["new-campaign"])
suspend fun newCampaign(id: String, bot: TelegramBot, user: User, update: CallbackQueryUpdate) = telegramApi {
    answerCallbackQuery(update.callbackQuery.id).send(user, bot)
    val advertiserService = get<AdvertiserService>()
    val advertiser = advertiserService.getAdvertiser(UUID.fromString(id)) ?: run {
        message { "Этого рекламодателя уже не существует! Может, вы нажали на старую кнопку?" }.send(user, bot)
        return@telegramApi
    }

    user["creating_campaign_advertiser_id"] = advertiser.id.toString()
    message { "Начинаем создание кампании. Введи заголовок рекламы." }.inlineKeyboardMarkup { cancelButton() }
        .send(user, bot)
    bot.inputListener.setChain(user, CreateCampaignChain.AdTitle)
}

internal suspend fun sendCampaignInfo(user: User, bot: TelegramBot, campaign: Campaign) {
    message {
        """
        ID кампании: ${campaign.id}
        Заголовок: ${campaign.adTitle}
        Текст: ${campaign.adText}
        Лимит показов: ${campaign.impressionsLimit}
        Лимит кликов: ${campaign.clicksLimit}
        Дата начала показа: ${campaign.startDate}
        Дата конца показа: ${campaign.endDate}
        Цена показа: ${campaign.costPerImpression}
        Цена клика: ${campaign.costPerClick}
        ID изображения: ${campaign.imageId?.toString() ?: "Пусто"}
        Таргетинг: ${
            campaign.targeting.let {
                "От ${it.ageFrom ?: "<пусто>"} до ${it.ageTo ?: "<пусто>"} лет, для пола ${it.gender ?: "<пусто>"}, для людей, находящихся в ${it.location ?: "<пусто>"}"
            }
        }
        """.trimIndent()
    }.inlineKeyboardMarkup {
        callbackData("✏\uFE0F Заголовок") { "c-title?id=${campaign.id}" }
        callbackData("✏\uFE0F Текст") { "c-text?id=${campaign.id}" }
        br()
        callbackData("✏\uFE0F Лимит показов") { "c-im-limit?id=${campaign.id}" }
        callbackData("✏\uFE0F Лимит кликов") { "c-cl-limit?id=${campaign.id}" }
        br()
        callbackData("✏\uFE0F Начало показа") { "c-st-date?id=${campaign.id}" }
        callbackData("✏\uFE0F Конец показа") { "c-end-date?id=${campaign.id}" }
        br()
        callbackData("✏\uFE0F Цена показа") { "c-im-cost?id=${campaign.id}" }
        callbackData("✏\uFE0F Цена клика") { "c-cl-cost?id=${campaign.id}" }
        br()
        callbackData("✏\uFE0F Изображение") { "c-image?id=${campaign.id}" }
        callbackData("✏\uFE0F Таргетинг") { "c-targeting?id=${campaign.id}" }
        br()
        callbackData("\uD83E\uDE84 Сгенерировать текст с помощью ИИ") { "c-ai-text?id=${campaign.id}" }
        br()
        callbackData("❌ Удалить кампанию") { "c-delete?id=${campaign.id}" }
    }.send(user, bot)
}

@CommandHandler.CallbackQuery(["edit-campaign"])
suspend fun editCampaign(id: String, user: User, bot: TelegramBot, update: CallbackQueryUpdate) = telegramApi {
    answerCallbackQuery(update.callbackQuery.id).send(user, bot)

    val id = UUID.fromString(id)
    val campaignService = get<CampaignService>()
    val campaign = campaignService.getCampaign(id) ?: run {
        message { "Этой кампании уже не существует! Может, ты кликнул на старую кнопку?" }.send(user, bot)
        return@telegramApi
    }

    sendCampaignInfo(user, bot, campaign)
}

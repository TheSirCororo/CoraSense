package ru.cororo.corasense.telegram.clients

import eu.vendeli.tgbot.TelegramBot
import eu.vendeli.tgbot.annotations.CommandHandler
import eu.vendeli.tgbot.api.answer.answerCallbackQuery
import eu.vendeli.tgbot.api.media.photo
import eu.vendeli.tgbot.api.message.message
import eu.vendeli.tgbot.types.User
import eu.vendeli.tgbot.types.internal.CallbackQueryUpdate
import eu.vendeli.tgbot.types.internal.ImplicitFile
import eu.vendeli.tgbot.utils.toInputFile
import org.koin.core.component.get
import ru.cororo.corasense.inject.telegramApi
import ru.cororo.corasense.model.action.data.AdAction
import ru.cororo.corasense.service.*
import java.util.*

private val clickActions = mutableMapOf<UUID, Pair<UUID, UUID>>()

@CommandHandler.CallbackQuery(["client-view-ad"])
suspend fun clientViewAd(id: String, user: User, bot: TelegramBot, update: CallbackQueryUpdate) = telegramApi {
    answerCallbackQuery(update.callbackQuery.id).send(user, bot)
    val clientService = get<ClientService>()
    val client = clientService.getClient(UUID.fromString(id)) ?: run {
        message { "Клиента уже не существует! Может, вы кликнули на старую кнопку?" }.send(user, bot)
        return@telegramApi
    }

    val campaignService = get<CampaignService>()
    val ad = campaignService.getRelevantCampaignForClient(client) ?: run {
        message { "Реклама закончилась :(" }.send(user, bot)
        return@telegramApi
    }

    val advertiserService = get<AdvertiserService>()
    val advertiser = advertiserService.getAdvertiser(ad.advertiserId) ?: return@telegramApi
    val imageService = get<ImageService>()
    val messageText = """
            Реклама ${ad.id}:
            Заголовок: ${ad.adTitle}
            Текст: ${ad.adText}
            Рекламодатель: ${advertiser.name} (${advertiser.id})
        """.trimIndent()

    var sent = false
    val clickActionId = UUID.randomUUID()
    clickActions[clickActionId] = client.id to ad.id
    if (ad.imageId != null) {
        val image = imageService.getImage(ad.imageId)
        if (image != null) {
            val imageData = imageService.loadImageBytes(image.id)
            if (imageData != null) {
                sent = true
                photo(ImplicitFile.InpFile(imageData.toInputFile("image.jpg", "image/jpeg")))
                    .caption { messageText }
                    .inlineKeyboardMarkup {
                        callbackData("⬆\uFE0F Кликнуть") { "click-ad?actionId=$clickActionId" }
                        callbackData("➡\uFE0F Следующая реклама") { "client-view-ad?id=${client.id}" }
                    }.send(user, bot)
            }
        }
    }

    if (!sent) {
        message { messageText }.inlineKeyboardMarkup {
            callbackData("⬆\uFE0F Кликнуть") { "click-ad?actionId=$clickActionId" }
            callbackData("➡\uFE0F Следующая реклама") { "client-view-ad?id=${client.id}" }
        }.send(user, bot)
    }

    val adActionService = get<AdActionService>()
    val currentDayService = get<CurrentDayService>()
    adActionService.saveAction(
        AdAction(
            UUID.randomUUID(),
            ad.advertiserId,
            ad.id,
            client.id,
            AdAction.Type.IMPRESSION,
            currentDayService.getCurrentDay(),
            ad.costPerImpression
        )
    )
}

@CommandHandler.CallbackQuery(["click-ad"])
suspend fun clickAd(actionId: String, user: User, bot: TelegramBot, update: CallbackQueryUpdate) = telegramApi {
    answerCallbackQuery(update.callbackQuery.id).send(user, bot)
    val (clientId, campaignId) = clickActions[UUID.fromString(actionId)] ?: run {
        message { "Эта кнопка уже неактуальна!" }.send(user, bot)
        return@telegramApi
    }

    val clientService = get<ClientService>()
    val client = clientService.getClient(clientId) ?: run {
        message { "Клиента уже не существует! Может, вы кликнули на старую кнопку?" }.send(user, bot)
        return@telegramApi
    }

    val campaignService = get<CampaignService>()
    val campaign = campaignService.getCampaign(campaignId) ?: run {
        message { "Кампании уже не существует! Может, вы кликнули на старую кнопку?" }.send(user, bot)
        return@telegramApi
    }

    val adActionService = get<AdActionService>()
    val currentDayService = get<CurrentDayService>()
    if (adActionService.reachedClickLimit(campaign.id) || !adActionService.hasImpressed(clientId, campaign.id)) {
        message { "Вы не можете кликнуть на эту рекламу. Достигнут лимит, либо вы не просматривали эту рекламу." }
            .send(user, bot)
        return@telegramApi
    }

    adActionService.saveAction(
        AdAction(
            UUID.randomUUID(),
            campaign.advertiserId,
            campaign.id,
            client.id,
            AdAction.Type.CLICK,
            currentDayService.getCurrentDay(),
            campaign.costPerImpression
        )
    )

    message { "Клик!" }.send(user, bot)
}

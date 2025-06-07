package ru.cororo.corasense.telegram.handler.campaigns

import eu.vendeli.tgbot.TelegramBot
import eu.vendeli.tgbot.annotations.CommandHandler
import eu.vendeli.tgbot.annotations.InputHandler
import eu.vendeli.tgbot.api.answer.answerCallbackQuery
import eu.vendeli.tgbot.api.media.photo
import eu.vendeli.tgbot.api.message.message
import eu.vendeli.tgbot.generated.get
import eu.vendeli.tgbot.generated.set
import eu.vendeli.tgbot.types.User
import eu.vendeli.tgbot.types.component.CallbackQueryUpdate
import eu.vendeli.tgbot.types.component.ImplicitFile
import eu.vendeli.tgbot.types.component.ProcessedUpdate
import eu.vendeli.tgbot.utils.common.toInputFile
import org.koin.core.component.get
import ru.cororo.corasense.shared.service.CampaignService
import ru.cororo.corasense.shared.service.ImageService
import ru.cororo.corasense.telegram.handler.cancel.cancelButton
import ru.cororo.corasense.telegram.util.readByteArray
import ru.cororo.corasense.telegram.util.telegramApi
import java.util.*

@CommandHandler.CallbackQuery(["c-image"])
suspend fun changeImage(id: String, user: User, bot: TelegramBot, update: CallbackQueryUpdate) = telegramApi {
    answerCallbackQuery(update.callbackQuery.id).send(user, bot)

    val id = UUID.fromString(id)
    val campaignService = get<CampaignService>()
    val campaign = campaignService.getCampaign(id) ?: run {
        message { "Этой кампании уже не существует! Может, ты кликнул на старую кнопку?" }.send(user, bot)
        return@telegramApi
    }

    val imageService = get<ImageService>()
    val imageId = campaign.imageId
    if (imageId != null) {
        val imageData = imageService.loadImageBytes(imageId)
        if (imageData != null) {
            photo(
                ImplicitFile.InpFile(
                    imageData.readByteArray().toInputFile(
                        "image.jpg",
                        "image/jpeg"
                    )
                )
            ).caption { "Текущее изображение." }.send(user, bot)
        }
    }

    message { "Введите UUID нового изображения для кампании." }.inlineKeyboardMarkup { cancelButton() }.send(user, bot)
    user["changing-campaign-id"] = campaign.id.toString()
    bot.inputListener[user] = "c-change-image"
}

@InputHandler(["c-change-image"])
suspend fun changeImageInput(user: User, bot: TelegramBot, update: ProcessedUpdate) = telegramApi {
    val id = UUID.fromString(user["changing-campaign-id"])
    val campaignService = get<CampaignService>()
    val campaign = campaignService.getCampaign(id) ?: run {
        message { "Этой кампании уже не существует! Может, ты кликнул на старую кнопку?" }.send(user, bot)
        return@telegramApi
    }

    val imageService = get<ImageService>()
    val imageId = try {
        UUID.fromString(update.text)
    } catch (_: Exception) {
        null
    }

    if (imageId == null || imageService.getImageData(imageId) == null) {
        message { "Некорректный UUID изображения или изображения с таким UUID не существует!" }
            .inlineKeyboardMarkup { cancelButton() }
            .send(user, bot)
        bot.inputListener[user] = "c-change-image"
        return@telegramApi
    }

    val newCampaign = campaign.copy(imageId = imageId)
    campaignService.saveCampaign(newCampaign)
    message { "Изображение рекламы успешно изменено!" }.inlineKeyboardMarkup {
        callbackData("Вернуться к редактированию кампании") { "edit-campaign?id=$id" }
    }.send(user, bot)
}

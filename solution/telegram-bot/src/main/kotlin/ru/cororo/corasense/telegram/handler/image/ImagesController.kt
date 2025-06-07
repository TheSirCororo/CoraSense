package ru.cororo.corasense.telegram.handler.image

import eu.vendeli.tgbot.TelegramBot
import eu.vendeli.tgbot.annotations.CommandHandler
import eu.vendeli.tgbot.annotations.InputHandler
import eu.vendeli.tgbot.api.answer.answerCallbackQuery
import eu.vendeli.tgbot.api.media.getFile
import eu.vendeli.tgbot.api.media.sendPhoto
import eu.vendeli.tgbot.api.message.message
import eu.vendeli.tgbot.types.User
import eu.vendeli.tgbot.types.component.*
import eu.vendeli.tgbot.utils.common.toInputFile
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import org.koin.core.component.get
import ru.cororo.corasense.shared.service.ImageService
import ru.cororo.corasense.telegram.handler.cancel.cancelButton
import ru.cororo.corasense.telegram.util.asBytesFlow
import ru.cororo.corasense.telegram.util.readByteArray
import ru.cororo.corasense.telegram.util.telegramApi
import java.util.*
import kotlin.time.Duration.Companion.seconds

private val httpClient = HttpClient(CIO)
private var maxImageSize: Long? = null

private suspend fun maxImageSize(): Long = telegramApi {
    if (maxImageSize == null) {
        maxImageSize = get<ImageService>().maxImageSize()
    }

    return@telegramApi maxImageSize!!
}

@CommandHandler(["/images"])
suspend fun images(user: User, bot: TelegramBot) {
    message { "\uD83D\uDDBC Управление изображениями" }.inlineKeyboardMarkup {
        callbackData("➕ Создать изображение") { "create-image" }
        br()
        callbackData("⏺\uFE0F Получить изображение по ID") { "get-image-by-id" }
    }.send(user, bot)
}

@CommandHandler.CallbackQuery(["create-image"])
suspend fun createImage(user: User, bot: TelegramBot, update: CallbackQueryUpdate) {
    answerCallbackQuery(update.callbackQuery.id).send(user, bot)

    message { "Отправьте изображение до 512 КБ." }.inlineKeyboardMarkup { cancelButton() }.send(user, bot)
    bot.inputListener[user] = "create-image"
}

@InputHandler(["create-image"])
suspend fun createImageInput(user: User, bot: TelegramBot, update: MessageUpdate) = telegramApi {
    suspend fun fail() {
        message { "Вы должны отправить изображение размером до 512 КБ." }.inlineKeyboardMarkup { cancelButton() }
            .send(user, bot)
        bot.inputListener[user] = "create-image"
    }

    val photos = update.message.photo ?: run {
        fail()
        return@telegramApi
    }

    val photo = photos.last()
    val imageService = get<ImageService>()
    if (photo.fileSize != null && photo.fileSize!!.toLong() > maxImageSize()) {
        fail()
        return@telegramApi
    }

    try {
        withTimeout(3.seconds) {
            val file = getFile(photo.fileId).sendReturning(bot).getOrNull() ?: run {
                fail()
                return@withTimeout
            }

            val imageBytes = httpClient.get(bot.getFileDirectUrl(file) ?: return@withTimeout).bodyAsBytes()
            val id = UUID.randomUUID()
            imageService.uploadImage(id, "image.jpg", imageBytes.asBytesFlow())
            imageService.saveImageData(id, "image.jpg")
            message { "Готово! ID: $id" }.send(user, bot)
        }
    } catch (_: TimeoutCancellationException) {
        message { "Не удалось скачать изображение. Повторите попытку." }.send(user, bot)
        fail()
    }
}

@CommandHandler.CallbackQuery(["get-image-by-id"])
suspend fun getImageById(user: User, bot: TelegramBot, update: CallbackQueryUpdate) {
    answerCallbackQuery(update.callbackQuery.id).send(user, bot)

    message { "Отправьте UUID изображения." }.inlineKeyboardMarkup { cancelButton() }.send(user, bot)
    bot.inputListener[user] = "get-image"
}

@InputHandler(["get-image"])
suspend fun getImageByIdInput(user: User, bot: TelegramBot, update: ProcessedUpdate) = telegramApi {
    val idString = update.text
    val id = try {
        UUID.fromString(idString)
    } catch (_: Exception) {
        message { "❌ Ты передал невалидный UUID! Попробуй снова." }.inlineKeyboardMarkup { cancelButton() }
            .send(user, bot)
        bot.inputListener[user] = "get-image"
        return@telegramApi
    }

    val imageService = get<ImageService>()
    val image = imageService.getImageData(id)
    if (image == null) {
        message { "❌ Ничего не найдено. Попробуй снова." }.inlineKeyboardMarkup { cancelButton() }.send(user, bot)
        bot.inputListener[user] = "get-image"
        return@telegramApi
    }

    val imageData = imageService.loadImageBytes(id)
        ?: run {
            message { "❌ Ничего не найдено. Попробуй снова." }.inlineKeyboardMarkup { cancelButton() }.send(user, bot)
            bot.inputListener[user] = "get-image"
            return@telegramApi
        }

    sendPhoto(ImplicitFile.InpFile(imageData.readByteArray().toInputFile("image.jpg", "image/jpeg"))).send(user, bot)
}

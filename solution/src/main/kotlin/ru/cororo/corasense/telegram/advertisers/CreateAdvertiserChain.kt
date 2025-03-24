package ru.cororo.corasense.telegram.advertisers

import eu.vendeli.tgbot.TelegramBot
import eu.vendeli.tgbot.annotations.InputChain
import eu.vendeli.tgbot.api.message.message
import eu.vendeli.tgbot.generated.getAllState
import eu.vendeli.tgbot.types.User
import eu.vendeli.tgbot.types.internal.BreakCondition
import eu.vendeli.tgbot.types.internal.ChainLink
import eu.vendeli.tgbot.types.internal.ProcessedUpdate
import eu.vendeli.tgbot.types.internal.chain.BaseStatefulLink
import org.koin.core.component.get
import ru.cororo.corasense.inject.telegramApi
import ru.cororo.corasense.model.advertiser.data.Advertiser
import ru.cororo.corasense.service.AdvertiserService
import ru.cororo.corasense.telegram.cancel.cancelButton
import ru.cororo.corasense.telegram.startKeyboard
import java.util.*

@InputChain
object CreateAdvertiserChain {
    object Uuid : BaseStatefulLink() {
        override val breakCondition: BreakCondition = BreakCondition { _, update, _ ->
            if (update.text == "Случайное значение") return@BreakCondition false

            return@BreakCondition try {
                UUID.fromString(update.text)
                false
            } catch (_: Exception) {
                true
            }
        }

        override suspend fun breakAction(user: User, update: ProcessedUpdate, bot: TelegramBot) {
            message { "Пожалуйста, укажи верный UUID или выбери случайное значение." }.inlineKeyboardMarkup {
                cancelButton()
            }.send(user, bot)
        }

        override suspend fun action(user: User, update: ProcessedUpdate, bot: TelegramBot): String {
            message { "Отлично!" }.replyKeyboardRemove().send(user, bot)
            message { "Введи имя рекламодателя." }.inlineKeyboardMarkup { cancelButton() }.send(user, bot)
            return if (update.text == "Случайное значение") UUID.randomUUID().toString() else update.text
        }
    }

    object Name : ChainLink() {
        override val breakCondition: BreakCondition = BreakCondition { _, update, _ -> update.text.length !in 3..128 }

        override suspend fun breakAction(user: User, update: ProcessedUpdate, bot: TelegramBot) {
            message { "Имя рекламодателя должно быть длиной от 3 до 128 символов." }.inlineKeyboardMarkup { cancelButton() }
                .send(user, bot)
        }

        override suspend fun action(user: User, update: ProcessedUpdate, bot: TelegramBot) = telegramApi {
            message { "Создаём..." }.replyKeyboardRemove().send(user, bot)

            val advertiserService = get<AdvertiserService>()
            val state = user.getAllState(CreateAdvertiserChain)
            val advertiser = Advertiser(UUID.fromString(state.Uuid), update.text)
            advertiserService.saveAdvertiser(advertiser)
            message { "✅ Твой рекламодатель создан.\nИмя: ${advertiser.name}\nID: ${advertiser.id}" }
                .inlineKeyboardMarkup {
                    callbackData("\uD83D\uDD27 Настройка") { "configure-advertiser?id=${advertiser.id}" }
                    callbackData("➕ Создать ещё") { "create-advertiser" }
                }.send(user, bot)

            message { "Выбери что делать дальше." }.startKeyboard()
        }
    }
}

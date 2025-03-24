package ru.cororo.corasense.telegram.clients

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
import ru.cororo.corasense.model.client.data.Client
import ru.cororo.corasense.service.ClientService
import ru.cororo.corasense.telegram.cancel.cancelButton
import ru.cororo.corasense.telegram.startKeyboard
import java.util.UUID

@InputChain
object CreateClientChain {
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
            message { "Введи логин клиента." }.inlineKeyboardMarkup { cancelButton() }.send(user, bot)
            return if (update.text == "Случайное значение") UUID.randomUUID().toString() else update.text
        }
    }

    object Login : BaseStatefulLink() {
        override val breakCondition: BreakCondition = BreakCondition { _, update, _ -> update.text.length !in 3..128 }

        override suspend fun breakAction(user: User, update: ProcessedUpdate, bot: TelegramBot) {
            message { "Логин клиента должен иметь длину от 3 до 128 символов." }.inlineKeyboardMarkup { cancelButton() }
                .send(user, bot)
        }

        override suspend fun action(
            user: User,
            update: ProcessedUpdate,
            bot: TelegramBot
        ): String {
            message { "Введи возраст клиента." }.inlineKeyboardMarkup { cancelButton() }.send(user, bot)
            return update.text
        }
    }

    object Age : BaseStatefulLink() {
        override val breakCondition: BreakCondition = BreakCondition { _, update, _ -> update.text.toIntOrNull().let { it ==  null || it !in 0..130 }}

        override suspend fun breakAction(user: User, update: ProcessedUpdate, bot: TelegramBot) {
            message { "Возраст клиента должен быть целым числом от 0 до 130." }.inlineKeyboardMarkup { cancelButton() }
                .send(user, bot)
        }

        override suspend fun action(user: User, update: ProcessedUpdate, bot: TelegramBot): String {
            message { "Выбери пол клиента." }.replyKeyboardMarkup {
                +"MALE"
                +"FEMALE"
            }.send(user, bot)
            return update.text.uppercase()
        }
    }

    object Gender : BaseStatefulLink() {
        override val breakCondition: BreakCondition = BreakCondition { _, update, _ ->
            try {
                Client.Gender.valueOf(update.text.uppercase())
                false
            } catch (_: Exception) {
                true
            }
        }

        override suspend fun breakAction(user: User, update: ProcessedUpdate, bot: TelegramBot) {
            message { "Введи male или female." }.inlineKeyboardMarkup { cancelButton() }.send(user, bot)
        }

        override suspend fun action(user: User, update: ProcessedUpdate, bot: TelegramBot): String {
            message { "Введи местоположение клиента." }.inlineKeyboardMarkup { cancelButton() }.send(user, bot)
            return update.text
        }
    }

    object Location : ChainLink() {
        override val breakCondition: BreakCondition = BreakCondition { _, update, _ -> update.text.length !in 3..128 }

        override suspend fun breakAction(user: User, update: ProcessedUpdate, bot: TelegramBot) {
            message { "Введи строку с местоположением от 3 до 128 символов." }.inlineKeyboardMarkup { cancelButton() }.send(user, bot)
        }

        override suspend fun action(
            user: User,
            update: ProcessedUpdate,
            bot: TelegramBot
        ) = telegramApi {
            message { "Готово! Создаём клиента..." }.startKeyboard().send(user, bot)
            val state = user.getAllState(CreateClientChain)
            val id = UUID.fromString(state.Uuid ?: "")
            val login = state.Login ?: ""
            val age = state.Age?.toInt() ?: 0
            val gender = Client.Gender.valueOf(state.Gender ?: "")
            val location = update.text
            val client = Client(id, login, age, location, gender)
            val clientService = get<ClientService>()
            clientService.saveClient(client)
            message { """
                ✅ Твой клиент создан.
                📔 ID: ${client.id}
                ✏️ Логин: ${client.login}
                🌿 Возраст: ${client.age}
                👨‍🦱 Пол: ${client.gender.russianName}
                🗺 Местоположение: ${client.location}
            """.trimIndent() }
                .inlineKeyboardMarkup {
                    callbackData("\uD83D\uDD27 Настройка") { "configure-client?id=${client.id}" }
                    callbackData("➕ Создать ещё") { "create-client" }
                }.send(user, bot)
        }

    }
}
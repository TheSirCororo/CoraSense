package ru.cororo.corasense.telegram.campaigns

import eu.vendeli.tgbot.TelegramBot
import eu.vendeli.tgbot.annotations.InputChain
import eu.vendeli.tgbot.api.message.message
import eu.vendeli.tgbot.generated.get
import eu.vendeli.tgbot.generated.getAllState
import eu.vendeli.tgbot.types.User
import eu.vendeli.tgbot.types.internal.BreakCondition
import eu.vendeli.tgbot.types.internal.ChainLink
import eu.vendeli.tgbot.types.internal.ProcessedUpdate
import eu.vendeli.tgbot.types.internal.chain.BaseStatefulLink
import org.koin.core.component.get
import ru.cororo.corasense.inject.telegramApi
import ru.cororo.corasense.model.campaign.data.Campaign
import ru.cororo.corasense.service.CampaignService
import ru.cororo.corasense.telegram.cancel.cancelButton
import java.util.*

@InputChain
object CampaignTargetingChain {
    object Gender : BaseStatefulLink() {
        override val breakCondition: BreakCondition = BreakCondition { _, update, _ ->
            try {
                Campaign.Targeting.Gender.valueOf(update.text.uppercase())
                false
            } catch (_: Exception) {
                true
            }
        }

        override suspend fun breakAction(user: User, update: ProcessedUpdate, bot: TelegramBot) {
            message { "Некорректный пол." }.inlineKeyboardMarkup { cancelButton() }.send(user, bot)
        }

        override suspend fun action(user: User, update: ProcessedUpdate, bot: TelegramBot): String {
            message { "Выбери минимальный возраст клиентов, которым ты хочешь показывать рекламу, либо выбери Пусто" }.replyKeyboardMarkup {
                +"Пусто"
            }.send(user, bot)
            return update.text
        }
    }

    object AgeFrom : BaseStatefulLink() {
        override val breakCondition: BreakCondition = BreakCondition { _, update, _ ->
            update.text.toIntOrNull().let { update.text != "Пусто" && (it == null || it !in 0..130) }
        }

        override suspend fun breakAction(user: User, update: ProcessedUpdate, bot: TelegramBot) {
            message { "Возраст должен быть целым неотрицательным числом от 0 до 130." }.inlineKeyboardMarkup { cancelButton() }
                .send(user, bot)
        }

        override suspend fun action(user: User, update: ProcessedUpdate, bot: TelegramBot): String {
            message { "Выбери максимальный возраст клиентов, которым ты хочешь показывать рекламу, либо выбери Пусто" }.replyKeyboardMarkup {
                +"Пусто"
            }.send(user, bot)

            return update.text
        }
    }

    object AgeTo : BaseStatefulLink() {
        override val breakCondition: BreakCondition = BreakCondition { _, update, _ ->
            update.text.toIntOrNull().let { update.text != "Пусто" && (it == null || it !in 0..130) }
        }

        override suspend fun breakAction(user: User, update: ProcessedUpdate, bot: TelegramBot) {
            message { "Возраст должен быть целым неотрицательным числом от 0 до 130." }.inlineKeyboardMarkup { cancelButton() }
                .send(user, bot)
        }

        override suspend fun action(user: User, update: ProcessedUpdate, bot: TelegramBot): String {
            message { "Выбери местоположение клиентов, которым ты хочешь показывать рекламу, либо выбери Пусто" }.replyKeyboardMarkup {
                +"Пусто"
            }.send(user, bot)

            return update.text
        }
    }

    object Location : ChainLink() {
        override val breakCondition: BreakCondition = BreakCondition { _, update, _ ->
            update.text.isBlank() || update.text.isEmpty()
        }

        override suspend fun breakAction(user: User, update: ProcessedUpdate, bot: TelegramBot) {
            message { "Местоположение не может быть пустым." }.inlineKeyboardMarkup { cancelButton() }.send(user, bot)
        }

        override suspend fun action(user: User, update: ProcessedUpdate, bot: TelegramBot) = telegramApi {
            val campaignId = UUID.fromString(user["editing-campaign-id"] ?: return@telegramApi)
            val campaignService = get<CampaignService>()
            val campaign = campaignService.getCampaign(campaignId) ?: run {
                message { "Этой кампании уже не существует! Может, ты кликнул на старую кнопку?" }.send(user, bot)
                return@telegramApi
            }

            val state = user.getAllState(CampaignTargetingChain)
            val targeting = Campaign.Targeting(
                gender = state.Gender?.let { Campaign.Targeting.Gender.valueOf(it) },
                ageFrom = state.AgeFrom?.let { if (it == "Пусто") null else it.toInt() },
                ageTo = state.AgeTo?.let { if (it == "Пусто") null else it.toInt() },
                location = if (update.text == "Пусто") null else update.text
            )

            val newCampaign = campaign.copy(targeting = targeting)
            campaignService.saveCampaign(newCampaign)
            message { "Таргетинг рекламы успешно изменён!" }.inlineKeyboardMarkup {
                callbackData("Вернуться к редактированию кампании") { "edit-campaign?id=$campaignId" }
            }.send(user, bot)
        }
    }
}
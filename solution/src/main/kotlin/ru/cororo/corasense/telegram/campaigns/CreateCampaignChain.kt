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
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import ru.cororo.corasense.inject.telegramApi
import ru.cororo.corasense.model.campaign.data.Campaign
import ru.cororo.corasense.model.campaign.dto.CampaignCreateRequest
import ru.cororo.corasense.model.moderation.data.ModerationScope
import ru.cororo.corasense.service.CampaignService
import ru.cororo.corasense.service.ImageService
import ru.cororo.corasense.service.ModerationService
import ru.cororo.corasense.telegram.cancel.cancelButton
import ru.cororo.corasense.telegram.startKeyboard
import java.util.*

@InputChain
object CreateCampaignChain {
    object AdTitle : BaseStatefulLink(), KoinComponent {
        private val moderationService = get<ModerationService>()
        override val breakCondition: BreakCondition = BreakCondition { _, update, _ ->
            update.text.length !in 1..256 ||
                    !moderationService.moderateIfNeed(ModerationScope.AD_TITLE, update.text).allowed
        }

        override suspend fun breakAction(user: User, update: ProcessedUpdate, bot: TelegramBot) {
            message { "Длина названия рекламы должна быть от 1 до 256 символов и должна проходить модерацию." }.inlineKeyboardMarkup { cancelButton() }
                .send(user, bot)
        }

        override suspend fun action(user: User, update: ProcessedUpdate, bot: TelegramBot): String {
            message { "Введи текст рекламы." }.inlineKeyboardMarkup { cancelButton() }.send(user, bot)
            return update.text
        }
    }

    object AdText : BaseStatefulLink(), KoinComponent {
        private val moderationService = get<ModerationService>()
        override val breakCondition: BreakCondition =
            BreakCondition { _, update, _ ->
                update.text.isBlank() || update.text.isEmpty() ||
                        !moderationService.moderateIfNeed(ModerationScope.AD_TITLE, update.text).allowed
            }

        override suspend fun breakAction(user: User, update: ProcessedUpdate, bot: TelegramBot) {
            message { "Текст рекламы не может быть пустым и должен проходить модерацию." }.inlineKeyboardMarkup { cancelButton() }
                .send(user, bot)
        }

        override suspend fun action(user: User, update: ProcessedUpdate, bot: TelegramBot): String {
            message { "Введи лимит показов рекламы." }.inlineKeyboardMarkup { cancelButton() }.send(user, bot)
            return update.text
        }
    }

    object ImpressionsLimit : BaseStatefulLink() {
        override val breakCondition: BreakCondition =
            BreakCondition { _, update, _ -> update.text.toIntOrNull().let { it == null || it < 0 } }

        override suspend fun breakAction(user: User, update: ProcessedUpdate, bot: TelegramBot) {
            message { "Количество показов должно быть целым неотрицательным числом." }.inlineKeyboardMarkup { cancelButton() }
                .send(user, bot)
        }

        override suspend fun action(user: User, update: ProcessedUpdate, bot: TelegramBot): String {
            message { "Введи лимит кликов рекламы." }.inlineKeyboardMarkup { cancelButton() }.send(user, bot)
            return update.text
        }
    }

    object ClicksLimit : BaseStatefulLink() {
        override val breakCondition: BreakCondition =
            BreakCondition { _, update, _ -> update.text.toIntOrNull().let { it == null || it < 0 } }

        override suspend fun breakAction(user: User, update: ProcessedUpdate, bot: TelegramBot) {
            message { "Количество кликов должно быть целым неотрицательным числом." }.inlineKeyboardMarkup { cancelButton() }
                .send(user, bot)
        }

        override suspend fun action(user: User, update: ProcessedUpdate, bot: TelegramBot): String {
            message { "Введи дату начала показа рекламы." }.inlineKeyboardMarkup { cancelButton() }.send(user, bot)
            return update.text
        }
    }

    object StartDate : BaseStatefulLink() {
        override val breakCondition: BreakCondition =
            BreakCondition { _, update, _ -> update.text.toIntOrNull().let { it == null || it < 0 } }

        override suspend fun breakAction(user: User, update: ProcessedUpdate, bot: TelegramBot) {
            message { "Дата начала показа должна быть целым неотрицательным числом." }.inlineKeyboardMarkup { cancelButton() }
                .send(user, bot)
        }

        override suspend fun action(user: User, update: ProcessedUpdate, bot: TelegramBot): String {
            message { "Введи дату конца показа рекламы." }.inlineKeyboardMarkup { cancelButton() }.send(user, bot)
            return update.text
        }
    }

    object EndDate : BaseStatefulLink() {
        override val breakCondition: BreakCondition =
            BreakCondition { _, update, _ -> update.text.toIntOrNull().let { it == null || it < 0 } }

        override suspend fun breakAction(user: User, update: ProcessedUpdate, bot: TelegramBot) {
            message { "Дата конца показа должна быть целым неотрицательным числом." }.inlineKeyboardMarkup { cancelButton() }
                .send(user, bot)
        }

        override suspend fun action(user: User, update: ProcessedUpdate, bot: TelegramBot): String {
            message { "Введи цену показа рекламы." }.inlineKeyboardMarkup { cancelButton() }.send(user, bot)
            return update.text
        }
    }

    object CostPerImpressions : BaseStatefulLink() {
        override val breakCondition: BreakCondition =
            BreakCondition { _, update, _ -> update.text.toDoubleOrNull().let { it == null || it < 0 } }

        override suspend fun breakAction(user: User, update: ProcessedUpdate, bot: TelegramBot) {
            message { "Цена показа должна быть неотрицательным вещественным числом." }.inlineKeyboardMarkup { cancelButton() }
                .send(user, bot)
        }

        override suspend fun action(user: User, update: ProcessedUpdate, bot: TelegramBot): String {
            message { "Введи цену клика рекламы." }.inlineKeyboardMarkup { cancelButton() }.send(user, bot)
            return update.text
        }
    }

    object CostPerClicks : BaseStatefulLink() {
        override val breakCondition: BreakCondition =
            BreakCondition { _, update, _ -> update.text.toDoubleOrNull().let { it == null || it < 0 } }

        override suspend fun breakAction(user: User, update: ProcessedUpdate, bot: TelegramBot) {
            message { "Цена кликов должна быть неотрицательным вещественным числом." }.inlineKeyboardMarkup { cancelButton() }
                .send(user, bot)
        }

        override suspend fun action(user: User, update: ProcessedUpdate, bot: TelegramBot): String {
            message { "Введи ID изображения (или выбери Пусто)." }.replyKeyboardMarkup {
                +"Пусто"
            }.send(user, bot)
            return update.text
        }
    }

    object ImageId : BaseStatefulLink(), KoinComponent {
        private val imageService = get<ImageService>()

        override val breakCondition: BreakCondition = BreakCondition { _, update, _ ->
            update.text.let {
                it != "Пусто" && try {
                    imageService.getImage(UUID.fromString(it)) == null
                } catch (_: Exception) {
                    true
                }
            }
        }

        override suspend fun breakAction(user: User, update: ProcessedUpdate, bot: TelegramBot) {
            message { "Укажи ID существующего изображения." }.inlineKeyboardMarkup { cancelButton() }.send(user, bot)
        }

        override suspend fun action(user: User, update: ProcessedUpdate, bot: TelegramBot): String {
            message {
                "Отлично! Теперь необходимо настроить таргетинг рекламы.\n" + "Выбери пол клиентов, которым хочешь показывать рекламу (MALE, FEMALE, ALL)."
            }.replyKeyboardMarkup {
                +"MALE"
                +"FEMALE"
                +"ALL"
            }.send(user, bot)
            return update.text
        }
    }

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
            message { "Готово! Подожди, создаём кампанию..." }.startKeyboard().send(user, bot)
            val state = user.getAllState(CreateCampaignChain)
            val title = state.AdTitle ?: return@telegramApi
            val text = state.AdText ?: return@telegramApi
            val impressionsLimit = state.ImpressionsLimit?.toInt() ?: return@telegramApi
            val clicksLimit = state.ClicksLimit?.toInt() ?: return@telegramApi
            val startDate = state.StartDate?.toInt() ?: return@telegramApi
            val endDate = state.EndDate?.toInt() ?: return@telegramApi
            val costPerImpressions = state.CostPerImpressions?.toDouble() ?: return@telegramApi
            val costPerClicks = state.CostPerClicks?.toDouble() ?: return@telegramApi
            val imageId = if (state.ImageId != "Пусто") UUID.fromString(state.ImageId!!) else null
            val targeting = CampaignCreateRequest.Targeting(
                gender = state.Gender?.let { Campaign.Targeting.Gender.valueOf(it) },
                ageFrom = state.AgeFrom?.let { if (it == "Пусто") null else it.toInt() },
                ageTo = state.AgeTo?.let { if (it == "Пусто") null else it.toInt() },
                location = if (update.text == "Пусто") null else update.text
            )
            val request = CampaignCreateRequest(
                adTitle = title,
                adText = text,
                impressionsLimit = impressionsLimit,
                clicksLimit = clicksLimit,
                startDate = startDate,
                endDate = endDate,
                costPerImpression = costPerImpressions,
                costPerClick = costPerClicks,
                imageId = imageId,
                targeting = targeting
            )

            val campaignService = get<CampaignService>()
            val creatingCampaignAdvertiserId = user["creating_campaign_advertiser_id"] ?: return@telegramApi
            val campaign = campaignService.createCampaign(UUID.fromString(creatingCampaignAdvertiserId), request)
            message {
                """ 
                ✅ Твоя кампания создана.
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
                callbackData("➕ Создать ещё") { "new-campaign?id=${campaign.advertiserId}" }
                callbackData("✏\uFE0F Настройка") { "edit-campaign?id=${campaign.id}" }
            }.send(user, bot)
        }
    }
}
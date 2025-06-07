package ru.cororo.corasense.moderation

import io.ktor.server.application.Application
import io.ktor.server.application.log
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import ru.cororo.corasense.model.moderation.data.ModerationVerdict
import ru.cororo.corasense.service.LlmService

object LlmModerationProvider : ModerationProvider, KoinComponent {
    val llmService by inject<LlmService>()
    val application by inject<Application>()

    override suspend fun moderate(text: String): ModerationVerdict =
        if (!llmService.isActive()) {
            application.log.warn("LLM сервис отключен. Отвечаем в модерации всегда allowed = true.")
            ModerationVerdict.Allowed
        } else {
            llmService.moderateText(text)
        }
}
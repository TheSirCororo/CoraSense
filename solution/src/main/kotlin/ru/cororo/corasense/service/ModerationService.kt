package ru.cororo.corasense.service

import io.ktor.server.application.Application
import io.ktor.server.application.log
import io.ktor.server.config.tryGetString
import io.ktor.server.config.tryGetStringList
import ru.cororo.corasense.model.moderation.data.ModerationScope
import ru.cororo.corasense.model.moderation.data.ModerationVerdict
import ru.cororo.corasense.moderation.BlacklistModerationProvider
import ru.cororo.corasense.moderation.LlmModerationProvider
import ru.cororo.corasense.moderation.ModerationProvider

class ModerationService(private val application: Application, private val llmService: LlmService) {
    private var enabled = application.environment.config.tryGetString("moderation.enabled")?.toBooleanStrictOrNull() == true
    private var mode = application.environment.config.tryGetString("moderation.mode")?.let {
        parseMode(it)
    } ?: Mode.BLACKLIST
    private val moderationScopes = application.environment.config.tryGetStringList("moderation.scopes")?.let {
        it.map {
            val split = it.split(":")
            val scopeString = split[0]
            val scopeMode = if (split.size > 1) {
                Mode.valueOf(split[1].uppercase())
            } else mode
            ModerationScope.valueOf(scopeString.uppercase()) to scopeMode
        }.associate { it.first to it.second }.toMutableMap()
    } ?: mutableMapOf()

    val moderationProviders = mapOf<Mode, ModerationProvider>(
        Mode.LLM to LlmModerationProvider,
        Mode.BLACKLIST to BlacklistModerationProvider
    )

    suspend fun moderateIfNeed(scope: ModerationScope, text: String): ModerationVerdict = if (enabled && scope in moderationScopes) {
        moderationProviders[moderationScopes[scope]]?.moderate(text)
    } else {
        ModerationVerdict.Allowed
    } ?: ModerationVerdict.Allowed

    enum class Mode {
        BLACKLIST,
        LLM
    }

    private fun parseMode(modeString: String) = if (!enabled) {
        Mode.BLACKLIST
    } else {
        try {
            val mode = Mode.valueOf(modeString.uppercase())
            if (mode == Mode.LLM && !llmService.isActive()) {
                application.log.warn("LLM отключено. Используем режим модерации BLACKLIST")
                Mode.BLACKLIST
            } else {
                mode
            }
        } catch (_: Exception) {
            application.log.warn("Не получилось спарсить режим модерации $modeString. Откатываемся к BLACKLIST")
            Mode.BLACKLIST
        }
    }

    fun setEnabled(state: Boolean) {
        enabled = state
        if (enabled) {
            application.log.info("Модерация включена.")
        } else {
            application.log.info("Модерация выключена.")
        }
    }

    fun setMode(scope: ModerationScope?, mode: Mode) {
        if (scope == null) {
            this.mode = mode
            application.log.info("Установлен общий режим модерации: $mode")
        } else {
            moderationScopes[scope] = mode
            application.log.info("Установлен режим модерации для $scope: $mode")
        }
    }
}
package ru.cororo.corasense.service

import io.ktor.server.application.Application
import io.ktor.server.application.log
import io.ktor.server.config.tryGetString
import io.ktor.server.config.tryGetStringList
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import ru.cororo.corasense.shared.model.moderation.ModerationScope
import ru.cororo.corasense.shared.model.moderation.ModerationVerdict
import ru.cororo.corasense.moderation.BlacklistModerationProvider
import ru.cororo.corasense.moderation.LlmModerationProvider
import ru.cororo.corasense.moderation.ModerationProvider
import ru.cororo.corasense.shared.model.moderation.ModerationMode
import ru.cororo.corasense.shared.service.ModerationService
import kotlin.coroutines.CoroutineContext

class ModerationServiceImpl(private val application: Application) :
    ModerationService, CoroutineScope {

    override val coroutineContext: CoroutineContext = Dispatchers.Default + CoroutineName("ModerationServiceImpl")

    private var enabled = application.environment.config.tryGetString("moderation.enabled")?.toBooleanStrictOrNull() == true
    private var mode = application.environment.config.tryGetString("moderation.mode")?.let {
        parseMode(it)
    } ?: ModerationMode.BLACKLIST
    private val moderationScopes = application.environment.config.tryGetStringList("moderation.scopes")?.let {
        it.map {
            val split = it.split(":")
            val scopeString = split[0]
            val scopeMode = if (split.size > 1) {
                ModerationMode.valueOf(split[1].uppercase())
            } else mode
            ModerationScope.valueOf(scopeString.uppercase()) to scopeMode
        }.associate { it.first to it.second }.toMutableMap()
    } ?: mutableMapOf()

    val moderationProviders = mapOf<ModerationMode, ModerationProvider>(
        ModerationMode.LLM to LlmModerationProvider,
        ModerationMode.BLACKLIST to BlacklistModerationProvider
    )

    override suspend fun moderateIfNeed(scope: ModerationScope, text: String): ModerationVerdict = if (enabled && scope in moderationScopes) {
        moderationProviders[moderationScopes[scope]]?.moderate(text)
    } else {
        ModerationVerdict.Allowed
    } ?: ModerationVerdict.Allowed

    private fun parseMode(modeString: String) = if (!enabled) {
        ModerationMode.BLACKLIST
    } else {
        try {
            ModerationMode.valueOf(modeString.uppercase())
        } catch (_: Exception) {
            application.log.warn("Не получилось спарсить режим модерации $modeString. Откатываемся к BLACKLIST")
            ModerationMode.BLACKLIST
        }
    }

    override suspend fun setEnabled(state: Boolean) {
        enabled = state
        if (enabled) {
            application.log.info("Модерация включена.")
        } else {
            application.log.info("Модерация выключена.")
        }
    }

    override suspend fun setMode(scope: ModerationScope?, mode: ModerationMode) {
        if (scope == null) {
            this.mode = mode
            application.log.info("Установлен общий режим модерации: $mode")
        } else {
            moderationScopes[scope] = mode
            application.log.info("Установлен режим модерации для $scope: $mode")
        }
    }
}
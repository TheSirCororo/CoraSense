package ru.cororo.corasense.model.moderation.dto

import kotlinx.serialization.Serializable
import ru.cororo.corasense.model.moderation.data.ModerationScope
import ru.cororo.corasense.service.ModerationService
import ru.cororo.corasense.validation.validator

@Serializable
data class ModerationModeRequest(
    val mode: String,
    val scope: String? = null
) {
    companion object {
        init {
            validator<ModerationModeRequest> {
                ModerationModeRequest::mode {
                    constrain("Доступные режимы модерации: ${ModerationService.Mode.entries.joinToString(", ")}") {
                        try {
                            ModerationService.Mode.valueOf(it.uppercase())
                            true
                        } catch (_: Exception) {
                            false
                        }
                    }
                }

                ModerationModeRequest::scope ifPresent {
                    constrain("Доступные области модерации: ${ModerationScope.entries.joinToString(", ")}") {
                        try {
                            ModerationScope.valueOf(it.uppercase())
                            true
                        } catch (_: Exception) {
                            false
                        }
                    }
                }
            }
        }
    }
}

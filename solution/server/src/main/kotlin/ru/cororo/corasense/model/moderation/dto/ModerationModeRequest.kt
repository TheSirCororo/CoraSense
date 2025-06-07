package ru.cororo.corasense.model.moderation.dto

import kotlinx.serialization.Serializable
import ru.cororo.corasense.shared.model.moderation.ModerationScope
import ru.cororo.corasense.shared.model.moderation.ModerationMode
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
                    constrain("Доступные режимы модерации: ${ModerationMode.entries.joinToString(", ")}") {
                        try {
                            ModerationMode.valueOf(it.uppercase())
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

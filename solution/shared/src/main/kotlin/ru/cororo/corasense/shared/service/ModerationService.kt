package ru.cororo.corasense.shared.service

import kotlinx.rpc.RemoteService
import kotlinx.rpc.annotations.Rpc
import ru.cororo.corasense.shared.model.moderation.ModerationMode
import ru.cororo.corasense.shared.model.moderation.ModerationScope
import ru.cororo.corasense.shared.model.moderation.ModerationVerdict

@Rpc
interface ModerationService : RemoteService {
    suspend fun moderateIfNeed(scope: ModerationScope, text: String): ModerationVerdict

    suspend fun setEnabled(state: Boolean)

    suspend fun setMode(scope: ModerationScope?, mode: ModerationMode)
}
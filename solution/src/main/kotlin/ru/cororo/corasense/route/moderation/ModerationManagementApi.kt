package ru.cororo.corasense.route.moderation

import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import org.koin.core.component.get
import ru.cororo.corasense.inject.api
import ru.cororo.corasense.model.dto.StatusResponse
import ru.cororo.corasense.model.dto.respondOk
import ru.cororo.corasense.model.moderation.data.ModerationScope
import ru.cororo.corasense.model.moderation.dto.ModerationModeRequest
import ru.cororo.corasense.route.Paths
import ru.cororo.corasense.service.ModerationService
import ru.cororo.corasense.util.post

fun Route.moderationManagementApi() = api {
    val moderationService = it.get<ModerationService>()
    post<Paths.Moderation.Enable>({
        summary = "Включить модерацию"
        description =
            "Включить модерацию в приложении. Это состояние не сохраняется и после перезапуска приложения значение будет взято из конфигурации."
    }) {
        moderationService.setEnabled(true)
        call.respondOk()
    }

    post<Paths.Moderation.Disable>({
        summary = "Выключить модерацию"
        description =
            "Выключить модерацию в приложении. Это состояние не сохраняется и после перезапуска приложения значение будет взято из конфигурации."
    }) {
        moderationService.setEnabled(false)
        call.respondOk()
    }

    post<Paths.Moderation.Mode>({
        summary = "Изменить режим модерации"
        description =
            "Изменить режим модерации. Поддерживается смена режима отдельно для scope. Поддерживаемые scope: ${
                ModerationScope.entries.joinToString(", ") { it.name }
            }. Поддерживаемые mode: ${ModerationService.Mode.entries.joinToString(", ") { it.name }}"
        request {
            body<ModerationModeRequest>()
        }

        response {
            code(HttpStatusCode.OK) {
                body<StatusResponse.Ok> {
                    example("OK") {
                        value = StatusResponse.Ok()
                    }
                }
            }
        }
    }) {
        val request = call.receive<ModerationModeRequest>()
        val mode = ModerationService.Mode.valueOf(request.mode.uppercase())
        val scope = request.scope?.let { ModerationScope.valueOf(it.uppercase()) }
        moderationService.setMode(scope, mode)
        call.respondOk()
    }
}
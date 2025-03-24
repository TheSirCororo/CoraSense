package ru.cororo.corasense.route.llm

import io.ktor.http.HttpStatusCode
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.Route
import org.koin.core.component.get
import ru.cororo.corasense.inject.api
import ru.cororo.corasense.model.dto.Errors
import ru.cororo.corasense.model.dto.respond
import ru.cororo.corasense.model.llm.dto.GenerateCampaignTextRequest
import ru.cororo.corasense.model.llm.dto.GenerateCampaignTextResponse
import ru.cororo.corasense.route.Paths
import ru.cororo.corasense.service.AdvertiserService
import ru.cororo.corasense.service.LlmService
import ru.cororo.corasense.util.post

fun Route.llmApi() = api {
    val llmService = it.get<LlmService>()
    val advertiserService = it.get<AdvertiserService>()

    post<Paths.Llm.GenerateCampaignText>({
        summary = "Генерация текста для кампании при помощи ИИ"
        request {
            body<GenerateCampaignTextRequest>()
        }

        response {
            code(HttpStatusCode.OK) {
                body<GenerateCampaignTextResponse>()
            }
        }
    }) {
        if (!llmService.isActive()) {
            Errors.LlmDisabled.respond()
        }

        val (advertiserId, campaignTitle) = call.receive<GenerateCampaignTextRequest>()
        val advertiser = advertiserService.getAdvertiser(advertiserId) ?: Errors.AdvertiserNotFound.respond()
        val text = llmService.generateCampaignText(campaignTitle, advertiser.name)
        call.respond(GenerateCampaignTextResponse(text))
    }
}
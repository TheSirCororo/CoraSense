package ru.cororo.corasense.plugin

import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.routing.routing
import kotlinx.rpc.krpc.ktor.server.Krpc
import kotlinx.rpc.krpc.ktor.server.rpc
import kotlinx.rpc.krpc.serialization.json.json
import org.koin.ktor.ext.get
import ru.cororo.corasense.shared.service.AdActionService
import ru.cororo.corasense.shared.service.AdvertiserService
import ru.cororo.corasense.shared.service.CampaignService
import ru.cororo.corasense.shared.service.ClientService
import ru.cororo.corasense.shared.service.CurrentDayService
import ru.cororo.corasense.shared.service.ImageService
import ru.cororo.corasense.shared.service.LlmService
import ru.cororo.corasense.shared.service.MLScoreService
import ru.cororo.corasense.shared.service.ModerationService

fun Application.configureRpc() {
    install(Krpc)

    routing {
        rpc("/rpc") {
            rpcConfig {
                serialization {
                    json()
                }
            }

            registerService<ClientService> { get() }
            registerService<AdvertiserService> { get() }
            registerService<CurrentDayService> { get() }
            registerService<CampaignService> { get() }
            registerService<MLScoreService> { get() }
            registerService<LlmService> { get() }
            registerService<ModerationService> { get() }
            registerService<ImageService> { get() }
            registerService<AdActionService> { get() }
        }
    }
}

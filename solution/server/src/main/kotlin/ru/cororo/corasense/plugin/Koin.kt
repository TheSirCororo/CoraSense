package ru.cororo.corasense.plugin

import io.ktor.server.application.*
import org.koin.core.context.stopKoin
import org.koin.core.module.Module
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger
import ru.cororo.corasense.repo.action.AdActionRepo
import ru.cororo.corasense.repo.action.CachedAdActionRepo
import ru.cororo.corasense.repo.action.DatabaseAdActionRepo
import ru.cororo.corasense.repo.advertiser.AdvertiserRepo
import ru.cororo.corasense.repo.advertiser.CachedAdvertiserRepo
import ru.cororo.corasense.repo.advertiser.DatabaseAdvertiserRepo
import ru.cororo.corasense.repo.campaign.CachedCampaignRepo
import ru.cororo.corasense.repo.campaign.CampaignRepo
import ru.cororo.corasense.repo.campaign.DatabaseCampaignRepo
import ru.cororo.corasense.repo.client.CachedClientRepo
import ru.cororo.corasense.repo.client.ClientRepo
import ru.cororo.corasense.repo.client.DatabaseClientRepo
import ru.cororo.corasense.repo.image.CachedImageRepo
import ru.cororo.corasense.repo.image.DatabaseImageRepo
import ru.cororo.corasense.repo.image.ImageRepo
import ru.cororo.corasense.repo.ml.CachedMLScoreRepo
import ru.cororo.corasense.repo.ml.DatabaseMLScoreRepo
import ru.cororo.corasense.repo.ml.MLScoreRepo
import ru.cororo.corasense.repo.moderation.BlockedWordRepo
import ru.cororo.corasense.repo.moderation.CachedBlockedWordRepo
import ru.cororo.corasense.repo.moderation.DatabaseBlockedWordRepo
import ru.cororo.corasense.service.*

val appModules = mutableListOf<Module>()

fun Application.configureKoin() {
    install(Koin) {
        slf4jLogger()
        appModules.add(0, module {
            single<Application> { this@configureKoin }
            single<ClientRepo> { CachedClientRepo(DatabaseClientRepo) }
            single<ClientService> { ClientService(get()) }
            single<AdvertiserRepo> { CachedAdvertiserRepo(DatabaseAdvertiserRepo) }
            single<AdvertiserService> { AdvertiserService(get(), get()) }
            single<MLScoreRepo> { CachedMLScoreRepo(DatabaseMLScoreRepo) }
            single<MLScoreService> { MLScoreService(get()) }
            single<CurrentDayService> { CurrentDayService() }
            single<CampaignRepo> { CachedCampaignRepo(DatabaseCampaignRepo) }
            single<CampaignService> { CampaignService(get(), get(), get(), get()) }
            single<AdActionRepo> { CachedAdActionRepo(DatabaseAdActionRepo) }
            single<AdActionService> { AdActionService(get(), get(), get()) }
            single<MicrometerService> { PrometheusMicrometerService }
            single<ImageRepo> { CachedImageRepo(DatabaseImageRepo) }
            single<ImageService> { ImageService(get(), get()) }
            single<LlmService> { LlmService(get()) }
            single<ModerationService> { ModerationService(get(), get()) }
            single<BlockedWordRepo> { CachedBlockedWordRepo(DatabaseBlockedWordRepo) }
            single<TelegramBotService> { TelegramBotService(get()) }
        })

        modules(appModules)
        allowOverride(true)
    }

    monitor.subscribe(ApplicationStopped) {
        stopKoin()
    }
}

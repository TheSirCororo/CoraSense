package ru.cororo.corasense.test

import io.kotest.assertions.ktor.client.shouldHaveStatus
import io.kotest.core.extensions.install
import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.testcontainers.JdbcDatabaseContainerExtension
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.comparables.shouldBeLessThan
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.config.*
import io.ktor.server.testing.*
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.koin.dsl.module
import org.testcontainers.containers.PostgreSQLContainer
import ru.cororo.corasense.model.action.data.AdActionStats
import ru.cororo.corasense.model.action.data.AdActionStatsDaily
import ru.cororo.corasense.model.advertiser.data.Advertiser
import ru.cororo.corasense.model.campaign.data.Campaign
import ru.cororo.corasense.model.campaign.dto.CampaignAd
import ru.cororo.corasense.model.campaign.dto.CampaignClickRequest
import ru.cororo.corasense.model.campaign.dto.CampaignCreateRequest
import ru.cororo.corasense.model.campaign.dto.CampaignUpdateRequest
import ru.cororo.corasense.model.client.data.Client
import ru.cororo.corasense.model.ml.data.MLScore
import ru.cororo.corasense.model.time.dto.TimeUpdateRequest
import ru.cororo.corasense.module
import ru.cororo.corasense.plugin.appModules
import ru.cororo.corasense.repo.action.AdActionTable
import ru.cororo.corasense.repo.advertiser.AdvertiserTable
import ru.cororo.corasense.repo.campaign.CampaignTable
import ru.cororo.corasense.repo.client.ClientTable
import ru.cororo.corasense.repo.ml.MLScoreTable
import ru.cororo.corasense.repo.time.CurrentDayTable
import ru.cororo.corasense.service.MicrometerService
import java.util.*
import kotlin.system.measureTimeMillis

class E2ETests : FunSpec({
    val postgres = PostgreSQLContainer<Nothing>("postgres:17.2")
    val ds = install(JdbcDatabaseContainerExtension(postgres)) {
        maximumPoolSize = 4
        idleTimeout = 10000
    }

    fun runTest(name: String, hikariEnabled: Boolean = false, block: suspend (HttpClient) -> Unit) {
        test(name) {
            appModules.add(module {
                single<MicrometerService> { NoopMicrometerService }
            })

            testApplication {
                environment {
                    config = MapApplicationConfig(
                        "database.jdbc_url" to ds.jdbcUrl,
                        "database.username" to ds.username,
                        "database.password" to ds.password,
                        "database.hikari_enabled" to "$hikariEnabled",
                        "telegram.enabled" to "false"
                    )
                }

                application {
                    module()
                    transaction {
                        CurrentDayTable.deleteAll()
                        AdActionTable.deleteAll()
                        CampaignTable.deleteAll()
                        MLScoreTable.deleteAll()
                        AdvertiserTable.deleteAll()
                        ClientTable.deleteAll()
                    }
                }

                val client = createClient {
                    install(ContentNegotiation) {
                        json()
                    }

                    defaultRequest {
                        contentType(ContentType.Application.Json)
                    }
                }

                block(client)
                client.post("/time/advance") {
                    setBody(TimeUpdateRequest(0))
                }
            }
        }
    }

    runTest("Ping") { client ->
        val response = client.get("/ping")
        response.shouldHaveStatus(200)
    }

    runTest("Client create/save") { client ->
        client.post("/clients/bulk") {
            setBody(mockedClients.values)
        }.apply {
            shouldHaveStatus(201)
            body<List<Client>>().shouldBe(mockedClients.values)
        }

        val randomId = mockedClients.keys.random()
        client.get("/clients/$randomId").apply {
            shouldHaveStatus(200)
            body<Client>().shouldBe(mockedClients[randomId])
        }

        client.post("/clients/bulk") {
            setBody(mockedClients.values.random().copy(login = "Cororo").let { listOf(it) })
        }.apply {
            shouldHaveStatus(201)
            val body = body<List<Client>>()
            body.shouldHaveSize(1)
            body.first().login.shouldBe("Cororo")
        }

        client.post("/clients/bulk") {
            setBody(mockedClients.values)
        }.apply {
            shouldHaveStatus(201)
            body<List<Client>>().shouldBe(mockedClients.values)
        }

        val anotherRandomId = mockedClients.keys.random()
        client.get("/clients/$anotherRandomId").apply {
            shouldHaveStatus(200)
            body<Client>().shouldBe(mockedClients[anotherRandomId])
        }

        client.post("/clients/bulk") {
            setBody(mockedClients.values.random().copy(login = "").let { listOf(it) })
        }.apply {
            shouldHaveStatus(400)
        }

        client.get("/clients/123").apply {
            shouldHaveStatus(400)
        }

        client.get("/clients/${UUID.randomUUID()}").apply {
            shouldHaveStatus(404)
        }
    }

    runTest("Advertiser create/save") { client ->
        client.post("/advertisers/bulk") {
            setBody(mockedAdvertisers.values)
        }.apply {
            shouldHaveStatus(201)
            body<List<Advertiser>>().shouldBe(mockedAdvertisers.values)
        }

        val randomId = mockedAdvertisers.keys.random()
        client.get("/advertisers/$randomId").apply {
            shouldHaveStatus(200)
            body<Advertiser>().shouldBe(mockedAdvertisers[randomId])
        }

        client.post("/advertisers/bulk") {
            setBody(mockedAdvertisers.values.random().copy(name = "TBank").let { listOf(it) })
        }.apply {
            shouldHaveStatus(201)
            val body = body<List<Advertiser>>()
            body.shouldHaveSize(1)
            body.first().name.shouldBe("TBank")
        }

        client.post("/advertisers/bulk") {
            setBody(mockedAdvertisers.values)
        }.apply {
            shouldHaveStatus(201)
            body<List<Advertiser>>().shouldBe(mockedAdvertisers.values)
        }

        val anotherRandomId = mockedAdvertisers.keys.random()
        client.get("/advertisers/$anotherRandomId").apply {
            shouldHaveStatus(200)
            body<Advertiser>().shouldBe(mockedAdvertisers[anotherRandomId])
        }

        client.post("/advertisers/bulk") {
            setBody(mockedAdvertisers.values.random().copy(name = "").let { listOf(it) })
        }.apply {
            shouldHaveStatus(400)
        }

        client.get("/advertisers/123").apply {
            shouldHaveStatus(400)
        }

        client.get("/advertisers/${UUID.randomUUID()}").apply {
            shouldHaveStatus(404)
        }
    }

    runTest("Campaigns CRUD") { client ->
        client.post("/clients/bulk") {
            setBody(mockedClients.values)
        }

        client.post("/advertisers/bulk") {
            setBody(mockedAdvertisers.values)
        }

        val campaigns = testCampaignRequests.map {
            client.post("/advertisers/${mockedAdvertisers.keys.random()}/campaigns") {
                setBody(it)
            }.run {
                shouldHaveStatus(201)
                body<Campaign>()
            }
        }

        campaigns.groupBy { it.advertiserId }.forEach { (advertiserId, advertiserCampaigns) ->
            client.get("/advertisers/$advertiserId/campaigns").apply {
                shouldHaveStatus(200)
                body<List<Campaign>>().shouldBe(advertiserCampaigns)
            }

            advertiserCampaigns.forEach {
                client.get("/advertisers/$advertiserId/campaigns/${it.id}").apply {
                    shouldHaveStatus(200)
                    body<Campaign>().shouldBe(it)
                }
            }
        }

        client.post("/time/advance") {
            setBody(TimeUpdateRequest(1000))
        }.apply {
            shouldHaveStatus(200)
        }

        client.post("/advertisers/${mockedAdvertisers.keys.random()}/campaigns") {
            setBody(testCampaignRequests.random())
        }.apply {
            shouldHaveStatus(400)
        }

        client.post("/time/advance") {
            setBody(TimeUpdateRequest(1))
        }.apply {
            shouldHaveStatus(200)
        }

        client.post("/advertisers/${mockedAdvertisers.keys.random()}/campaigns") {
            setBody(testCampaignRequests.random().copy(endDate = 0))
        }.apply {
            shouldHaveStatus(400)
        }

        client.get("/advertisers/${mockedAdvertisers.keys.random()}/campaigns?size=0&page=100").apply {
            shouldHaveStatus(200)
            body<List<Campaign>>().shouldHaveSize(0)
        }

        client.get("/advertisers/${UUID.randomUUID()}/campaigns").apply {
            shouldHaveStatus(404)
        }

        client.get("/advertisers/${mockedAdvertisers.keys.random()}/campaigns/${UUID.randomUUID()}").apply {
            shouldHaveStatus(404)
        }

        val modifiedCampaign = campaigns.random()
            .copy(adText = "АЛЕКСАНДР ШАХОВ Я ВАШ ФАНАТ", targeting = Campaign.Targeting()) // таргетинг - синпобеда.рф
        client.put("/advertisers/${modifiedCampaign.advertiserId}/campaigns/${modifiedCampaign.id}") {
            setBody(
                CampaignUpdateRequest(
                    costPerImpression = modifiedCampaign.costPerImpression,
                    costPerClick = modifiedCampaign.costPerClick,
                    adTitle = modifiedCampaign.adTitle,
                    adText = modifiedCampaign.adText,
                    impressionsLimit = modifiedCampaign.impressionsLimit,
                    clicksLimit = modifiedCampaign.clicksLimit,
                    startDate = modifiedCampaign.startDate,
                    endDate = modifiedCampaign.endDate,
                    imageId = modifiedCampaign.imageId,
                    targeting = modifiedCampaign.targeting.let {
                        CampaignCreateRequest.Targeting(
                            it.gender, it.ageFrom, it.ageTo, it.location
                        )
                    }
                )
            )
        }.apply {
            shouldHaveStatus(200)
            body<Campaign>().shouldBe(modifiedCampaign)
        }

        campaigns.forEach {
            client.delete("/advertisers/${it.advertiserId}/campaigns/${it.id}").apply {
                shouldHaveStatus(204)
            }
        }

        client.get("/advertisers/${modifiedCampaign.advertiserId}/campaigns/${modifiedCampaign.id}").apply {
            shouldHaveStatus(404)
        }
    }

    runTest("Ad Actions") { client ->
        client.post("/time/advance") {
            setBody(TimeUpdateRequest(1))
        }

        client.post("/clients/bulk") {
            setBody(actionClients)
        }

        client.post("/advertisers/bulk") {
            setBody(actionAdvertisers)
        }

        val firstClient = actionClients[0]
        val (firstAdvertiser, secondAdvertiser, thirdAdvertiser) = actionAdvertisers
        val (firstCampaign, secondCampaign) = actionCampaignRequests.take(2).map {
            client.post("/advertisers/${firstAdvertiser.id}/campaigns") {
                setBody(it)
            }.body<Campaign>()
        }

        client.get("/ads?client_id=${firstClient.id}").apply {
            shouldHaveStatus(200)
            body<CampaignAd>().adId.shouldBe(secondCampaign.id) // Имеет цены за клики в 10000 раз выше
        }

        client.post("/ml-scores") {
            setBody(
                MLScore(
                    firstClient.id,
                    firstAdvertiser.id,
                    100
                )
            )
        }

        client.get("/ads?client_id=${firstClient.id}").apply {
            shouldHaveStatus(200)
            body<CampaignAd>().adId.shouldBe(firstCampaign.id) // Результаты изменятся, поскольку предыдущая реклама уже была активирована
        }

        val thirdCampaign =
            client.post("/advertisers/${secondAdvertiser.id}/campaigns") {
                setBody(actionCampaignRequests[2])
            }.body<Campaign>()

        client.get("/ads?client_id=${firstClient.id}").apply {
            shouldHaveStatus(200)
            body<CampaignAd>().adId.shouldBe(thirdCampaign.id) // Результаты изменятся, поскольку появится непросмотренная реклама
        }

        client.post("/ml-scores") {
            setBody(
                MLScore(
                    firstClient.id,
                    secondAdvertiser.id,
                    2410
                )
            )
        }

        client.get("/ads?client_id=${firstClient.id}").apply {
            shouldHaveStatus(200)
            body<CampaignAd>().adId.shouldBe(secondCampaign.id) // Все уже просмотрены, но цена имеет больше значения
        }

        client.post("/ml-scores") {
            setBody(
                MLScore(
                    firstClient.id,
                    firstAdvertiser.id,
                    240
                )
            )
        }

        client.get("/ads?client_id=${firstClient.id}").apply {
            shouldHaveStatus(200)
            body<CampaignAd>().adId.shouldBe(secondCampaign.id) // Теперь ML скор снова выше у первого рекламодателя, отдаём предпочтение ему
        }

        client.post("/ads/${firstCampaign.id}/click") {
            setBody(CampaignClickRequest(firstClient.id))
        }.apply {
            shouldHaveStatus(204)
        }

        client.post("/ads/${UUID.randomUUID()}/click") {
            setBody(CampaignClickRequest(firstClient.id))
        }.apply {
            shouldHaveStatus(404)
        }

        client.post("/advertisers/${thirdAdvertiser.id}/campaigns") {
            setBody(actionCampaignRequests[3])
        }.body<Campaign>()

        actionCampaignRequests.subList(4, 7).map {
            client.post("/advertisers/${thirdAdvertiser.id}/campaigns") {
                setBody(it)
            }.body<Campaign>()
        }

        client.post("/ml-scores") {
            setBody(
                MLScore(
                    firstClient.id,
                    thirdAdvertiser.id,
                    200
                )
            )
        }

        val actualThirdAds = mutableListOf<CampaignAd>()
        repeat(3) {
            client.get("/ads?client_id=${firstClient.id}").apply {
                shouldHaveStatus(200)
                val ad = body<CampaignAd>()
                ad.should { it !in actualThirdAds }
                actualThirdAds += ad
            }
        }
    }

    runTest("Ad actions (limits)") { client ->
        client.post("/time/advance") {
            setBody(TimeUpdateRequest(1))
        }

        client.post("/clients/bulk") {
            setBody(actionClients)
        }

        client.post("/advertisers/bulk") {
            setBody(actionAdvertisers)
        }

        val firstClient = actionClients.first()
        val firstAdvertiser = actionAdvertisers.first()
        val campaign = actionCampaignRequests.first().let {
            client.post("/advertisers/${firstAdvertiser.id}/campaigns") {
                setBody(it.copy(impressionsLimit = 2, clicksLimit = 1, adTitle = "Крутая реклама для теста лимитов"))
            }.body<Campaign>()
        }

        val ad = client.get("/ads?client_id=${firstClient.id}").run {
            shouldHaveStatus(200)
            body<CampaignAd>()
        }

        ad.adId.shouldBe(campaign.id)

        client.get("/ads?client_id=${firstClient.id}").run {
            shouldHaveStatus(200)
            body<CampaignAd>().shouldBe(ad)
        }

        val secondClient = actionClients[1]
        client.get("/ads?client_id=${secondClient.id}").run {
            shouldHaveStatus(200)
            body<CampaignAd>().shouldBe(ad)
        }

        // Хотя лимит и достигнут, клиент уже видел рекламу, нет смысла её скрывать от него
        client.get("/ads?client_id=${secondClient.id}").run {
            shouldHaveStatus(200)
        }

        // А вот третий клиент её не видел, а лимит уже достигнут. Не показываем
        val thirdClient = actionClients[2]
        client.get("/ads?client_id=${thirdClient.id}").run {
            shouldHaveStatus(404)
        }

        client.post("/ads/${ad.adId}/click") {
            setBody(CampaignClickRequest(firstClient.id))
        }.apply {
            shouldHaveStatus(204)
        }
    }

    runTest("Ad actions (targeting)") { client ->
        client.post("/time/advance") {
            setBody(TimeUpdateRequest(1))
        }

        client.post("/clients/bulk") {
            setBody(actionClients)
        }

        client.post("/advertisers/bulk") {
            setBody(actionAdvertisers)
        }

        val advertiser = actionAdvertisers.first()
        val (firstClient, secondClient) = actionClients
        val firstCampaign = targetingTestCampaignRequests[0].let {
            client.post("/advertisers/${advertiser.id}/campaigns") {
                setBody(it)
            }.body<Campaign>()
        }

        client.get("/ads?client_id=${firstClient.id}").apply {
            body<CampaignAd>().adId.shouldBe(firstCampaign.id)
        }

        client.get("/ads?client_id=${secondClient.id}").apply {
            body<CampaignAd>().adId.shouldBe(firstCampaign.id)
        }

        val secondCampaign = targetingTestCampaignRequests[1].let {
            client.post("/advertisers/${advertiser.id}/campaigns") {
                setBody(it)
            }.body<Campaign>()
        }

        client.get("/ads?client_id=${firstClient.id}").apply {
            body<CampaignAd>().adId.shouldBe(firstCampaign.id)
        }

        client.get("/ads?client_id=${secondClient.id}").apply {
            body<CampaignAd>().adId.shouldBe(secondCampaign.id)
        }

        val thirdCampaign = targetingTestCampaignRequests[2].let {
            client.post("/advertisers/${advertiser.id}/campaigns") {
                setBody(it)
            }.body<Campaign>()
        }

        client.get("/ads?client_id=${firstClient.id}").apply {
            body<CampaignAd>().adId.shouldBe(thirdCampaign.id)
        }

        client.get("/ads?client_id=${secondClient.id}").apply {
            body<CampaignAd>().adId.shouldNotBe(thirdCampaign.id)
        }

        val fourthCampaign = targetingTestCampaignRequests[3].let {
            client.post("/advertisers/${advertiser.id}/campaigns") {
                setBody(it)
            }.body<Campaign>()
        }

        client.get("/ads?client_id=${firstClient.id}").apply {
            body<CampaignAd>().adId.shouldNotBe(fourthCampaign.id)
        }

        client.get("/ads?client_id=${secondClient.id}").apply {
            body<CampaignAd>().adId.shouldNotBe(fourthCampaign.id)
        }

        val fifthCampaign = targetingTestCampaignRequests[4].let {
            client.post("/advertisers/${advertiser.id}/campaigns") {
                setBody(it)
            }.body<Campaign>()
        }

        client.get("/ads?client_id=${firstClient.id}").apply {
            body<CampaignAd>().adId.shouldNotBe(fifthCampaign.id)
        }

        client.get("/ads?client_id=${secondClient.id}").apply {
            body<CampaignAd>().adId.shouldNotBe(fifthCampaign.id)
        }

        val sixthCampaign = targetingTestCampaignRequests[5].let {
            client.post("/advertisers/${advertiser.id}/campaigns") {
                setBody(it)
            }.body<Campaign>()
        }

        client.get("/ads?client_id=${firstClient.id}").apply {
            body<CampaignAd>().adId.shouldNotBe(sixthCampaign.id)
        }

        client.get("/ads?client_id=${secondClient.id}").apply {
            body<CampaignAd>().adId.shouldBe(sixthCampaign.id)
        }

        val seventhCampaign = targetingTestCampaignRequests[6].let {
            client.post("/advertisers/${advertiser.id}/campaigns") {
                setBody(it)
            }.body<Campaign>()
        }

        client.get("/ads?client_id=${firstClient.id}").apply {
            body<CampaignAd>().adId.shouldNotBe(seventhCampaign.id)
        }

        client.get("/ads?client_id=${secondClient.id}").apply {
            body<CampaignAd>().adId.shouldNotBe(seventhCampaign.id)
        }
    }

    runTest("Ad Stats") { client ->
        client.post("/clients/bulk") {
            setBody(actionClients)
        }

        client.post("/advertisers/bulk") {
            setBody(actionAdvertisers)
        }

        val firstClient = client.get("/clients/${actionClients[2].id}").run {
            shouldHaveStatus(200)
            body<Client>()
        }

        val (firstAdvertiser, _) = client.post("/advertisers/bulk") {
            setBody(listOf(actionAdvertisers[3], actionAdvertisers[4]))
        }.run {
            shouldHaveStatus(201)
            body<List<Advertiser>>()
        }

        val firstCampaigns = actionCampaignRequests.map {
            client.post("/advertisers/${firstAdvertiser.id}/campaigns") {
                setBody(it)
            }.run {
                shouldHaveStatus(201)
                body<Campaign>()
            }
        }

        client.post("/ml-scores") {
            setBody(
                MLScore(
                    firstClient.id,
                    firstAdvertiser.id,
                    20000
                )
            )
        }

        client.post("/time/advance") {
            setBody(TimeUpdateRequest(2))
        }

        val ad = client.get("/ads?client_id=${firstClient.id}").run {
            shouldHaveStatus(200)
            body<CampaignAd>()
        }

        val campaign = firstCampaigns.find { it.id == ad.adId } ?: error("")
        client.get("/stats/campaigns/${ad.adId}").apply {
            shouldHaveStatus(200)
            val stats = body<AdActionStats>()

            stats.shouldBe(AdActionStats(1, 0, 0.0, campaign.costPerImpression, 0.0, campaign.costPerImpression))
        }

        client.get("/stats/campaigns/${ad.adId}/daily").apply {
            shouldHaveStatus(200)
            val stats = body<List<AdActionStatsDaily>>()

            stats.shouldBe(
                listOf(
                    AdActionStatsDaily(0, 0, 0.0, 0.0, 0.0, 0.0, 0),
                    AdActionStatsDaily(0, 0, 0.0, 0.0, 0.0, 0.0, 1),
                    AdActionStatsDaily(1, 0, 0.0, campaign.costPerImpression, 0.0, campaign.costPerImpression, 2),
                )
            )
        }

        client.get("/stats/advertisers/${firstAdvertiser.id}/campaigns").apply {
            shouldHaveStatus(200)
            val stats = body<AdActionStats>()

            stats.shouldBe(AdActionStats(1, 0, 0.0, campaign.costPerImpression, 0.0, campaign.costPerImpression))
        }

        client.get("/stats/advertisers/${firstAdvertiser.id}/campaigns/daily").apply {
            shouldHaveStatus(200)
            val stats = body<List<AdActionStatsDaily>>()

            stats.shouldBe(
                listOf(
                    AdActionStatsDaily(0, 0, 0.0, 0.0, 0.0, 0.0, 0),
                    AdActionStatsDaily(0, 0, 0.0, 0.0, 0.0, 0.0, 1),
                    AdActionStatsDaily(1, 0, 0.0, campaign.costPerImpression, 0.0, campaign.costPerImpression, 2),
                )
            )
        }

        client.post("/ads/${ad.adId}/click") {
            setBody(CampaignClickRequest(firstClient.id))
        }

        client.get("/stats/campaigns/${ad.adId}").apply {
            shouldHaveStatus(200)
            val stats = body<AdActionStats>()

            stats.shouldBe(
                AdActionStats(
                    1,
                    1,
                    100.0,
                    campaign.costPerImpression,
                    campaign.costPerClick,
                    campaign.costPerImpression + campaign.costPerClick
                )
            )
        }

        client.get("/stats/campaigns/${ad.adId}/daily").apply {
            shouldHaveStatus(200)
            val stats = body<List<AdActionStatsDaily>>()

            stats.shouldBe(
                listOf(
                    AdActionStatsDaily(0, 0, 0.0, 0.0, 0.0, 0.0, 0),
                    AdActionStatsDaily(0, 0, 0.0, 0.0, 0.0, 0.0, 1),
                    AdActionStatsDaily(
                        1,
                        1,
                        100.0,
                        campaign.costPerImpression,
                        campaign.costPerClick,
                        campaign.costPerImpression + campaign.costPerClick,
                        2
                    )
                )
            )
        }

        client.get("/stats/advertisers/${firstAdvertiser.id}/campaigns").apply {
            shouldHaveStatus(200)
            val stats = body<AdActionStats>()

            stats.shouldBe(
                AdActionStats(
                    1,
                    1,
                    100.0,
                    campaign.costPerImpression,
                    campaign.costPerClick,
                    campaign.costPerImpression + campaign.costPerClick
                )
            )
        }

        client.get("/stats/advertisers/${firstAdvertiser.id}/campaigns/daily").apply {
            shouldHaveStatus(200)
            val stats = body<List<AdActionStatsDaily>>()

            stats.shouldBe(
                listOf(
                    AdActionStatsDaily(0, 0, 0.0, 0.0, 0.0, 0.0, 0),
                    AdActionStatsDaily(0, 0, 0.0, 0.0, 0.0, 0.0, 1),
                    AdActionStatsDaily(
                        1,
                        1,
                        100.0,
                        campaign.costPerImpression,
                        campaign.costPerClick,
                        campaign.costPerImpression + campaign.costPerClick,
                        2
                    )
                )
            )
        }

        client.post("/time/advance") {
            setBody(TimeUpdateRequest(3))
        }

        client.get("/stats/campaigns/${ad.adId}").apply {
            shouldHaveStatus(200)
            val stats = body<AdActionStats>()

            stats.shouldBe(
                AdActionStats(
                    1,
                    1,
                    100.0,
                    campaign.costPerImpression,
                    campaign.costPerClick,
                    campaign.costPerImpression + campaign.costPerClick
                )
            )
        }

        client.get("/stats/campaigns/${ad.adId}/daily").apply {
            shouldHaveStatus(200)
            val stats = body<List<AdActionStatsDaily>>()

            stats.shouldBe(
                listOf(
                    AdActionStatsDaily(0, 0, 0.0, 0.0, 0.0, 0.0, 0),
                    AdActionStatsDaily(0, 0, 0.0, 0.0, 0.0, 0.0, 1),
                    AdActionStatsDaily(
                        1,
                        1,
                        100.0,
                        campaign.costPerImpression,
                        campaign.costPerClick,
                        campaign.costPerImpression + campaign.costPerClick,
                        2
                    ),
                    AdActionStatsDaily(0, 0, 0.0, 0.0, 0.0, 0.0, 3)
                )
            )
        }

        client.get("/stats/advertisers/${firstAdvertiser.id}/campaigns").apply {
            shouldHaveStatus(200)
            val stats = body<AdActionStats>()

            stats.shouldBe(
                AdActionStats(
                    1,
                    1,
                    100.0,
                    campaign.costPerImpression,
                    campaign.costPerClick,
                    campaign.costPerImpression + campaign.costPerClick
                )
            )
        }

        client.get("/stats/advertisers/${firstAdvertiser.id}/campaigns/daily").apply {
            shouldHaveStatus(200)
            val stats = body<List<AdActionStatsDaily>>()

            stats.shouldBe(
                listOf(
                    AdActionStatsDaily(0, 0, 0.0, 0.0, 0.0, 0.0, 0),
                    AdActionStatsDaily(0, 0, 0.0, 0.0, 0.0, 0.0, 1),
                    AdActionStatsDaily(
                        1,
                        1,
                        100.0,
                        campaign.costPerImpression,
                        campaign.costPerClick,
                        campaign.costPerImpression + campaign.costPerClick,
                        2
                    ),
                    AdActionStatsDaily(0, 0, 0.0, 0.0, 0.0, 0.0, 3)
                )
            )
        }

        val secondAd = client.get("/ads?client_id=${firstClient.id}").run {
            shouldHaveStatus(200)
            body<CampaignAd>()
        }

        if (ad.adId == secondAd.adId) error("")

        val secondCampaign = firstCampaigns.find { it.id == secondAd.adId } ?: error("")
        client.get("/stats/campaigns/${ad.adId}").apply {
            shouldHaveStatus(200)
            val stats = body<AdActionStats>()

            stats.shouldBe(
                AdActionStats(
                    1,
                    1,
                    100.0,
                    campaign.costPerImpression,
                    campaign.costPerClick,
                    campaign.costPerImpression + campaign.costPerClick
                )
            )
        }

        client.get("/stats/campaigns/${ad.adId}/daily").apply {
            shouldHaveStatus(200)
            val stats = body<List<AdActionStatsDaily>>()

            stats.shouldBe(
                listOf(
                    AdActionStatsDaily(0, 0, 0.0, 0.0, 0.0, 0.0, 0),
                    AdActionStatsDaily(0, 0, 0.0, 0.0, 0.0, 0.0, 1),
                    AdActionStatsDaily(
                        1,
                        1,
                        100.0,
                        campaign.costPerImpression,
                        campaign.costPerClick,
                        campaign.costPerImpression + campaign.costPerClick,
                        2
                    ),
                    AdActionStatsDaily(0, 0, 0.0, 0.0, 0.0, 0.0, 3)
                )
            )
        }

        client.get("/stats/campaigns/${secondAd.adId}").apply {
            shouldHaveStatus(200)
            val stats = body<AdActionStats>()

            stats.shouldBe(
                AdActionStats(
                    1,
                    0,
                    0.0,
                    secondCampaign.costPerImpression,
                    0.0,
                    secondCampaign.costPerImpression
                )
            )
        }

        client.get("/stats/campaigns/${secondAd.adId}/daily").apply {
            shouldHaveStatus(200)
            val stats = body<List<AdActionStatsDaily>>()

            stats.shouldBe(
                listOf(
                    AdActionStatsDaily(0, 0, 0.0, 0.0, 0.0, 0.0, 0),
                    AdActionStatsDaily(0, 0, 0.0, 0.0, 0.0, 0.0, 1),
                    AdActionStatsDaily(0, 0, 0.0, 0.0, 0.0, 0.0, 2),
                    AdActionStatsDaily(
                        1,
                        0,
                        0.0,
                        secondCampaign.costPerImpression,
                        0.0,
                        secondCampaign.costPerImpression,
                        3
                    ),
                )
            )
        }

        client.get("/stats/advertisers/${firstAdvertiser.id}/campaigns").apply {
            shouldHaveStatus(200)
            val stats = body<AdActionStats>()

            stats.shouldBe(
                AdActionStats(
                    2,
                    1,
                    50.0,
                    campaign.costPerImpression + secondCampaign.costPerImpression,
                    campaign.costPerClick,
                    campaign.costPerImpression + secondCampaign.costPerImpression + campaign.costPerClick
                )
            )
        }

        client.get("/stats/advertisers/${firstAdvertiser.id}/campaigns/daily").apply {
            shouldHaveStatus(200)
            val stats = body<List<AdActionStatsDaily>>()

            stats.shouldBe(
                listOf(
                    AdActionStatsDaily(0, 0, 0.0, 0.0, 0.0, 0.0, 0),
                    AdActionStatsDaily(0, 0, 0.0, 0.0, 0.0, 0.0, 1),
                    AdActionStatsDaily(
                        1,
                        1,
                        100.0,
                        campaign.costPerImpression,
                        campaign.costPerClick,
                        campaign.costPerImpression + campaign.costPerClick,
                        2
                    ),
                    AdActionStatsDaily(
                        1,
                        0,
                        0.0,
                        secondCampaign.costPerImpression,
                        0.0,
                        secondCampaign.costPerImpression,
                        3
                    )
                )
            )
        }
    }

    runTest("Test time of GET /ads", hikariEnabled = true) { client ->
        client.post("/clients/bulk") {
            setBody(actionClients)
        }

        client.post("/advertisers/bulk") {
            setBody(actionAdvertisers)
        }

        client.post("/time/advance") {
            setBody(TimeUpdateRequest(1))
        }

        actionAdvertisers.forEach { advertiser ->
            actionCampaignRequests.map {
                client.post("/advertisers/${advertiser.id}/campaigns") {
                    setBody(it)
                }.run {
                    shouldHaveStatus(201)
                    body<Campaign>()
                }
            }
        }

        val times = mutableListOf<Long>()
        repeat(1000) {
            times.add(measureTimeMillis {
                client.get("/ads?client_id=${actionClients.first().id}").bodyAsText()
            })
        }

        val avg = times.average()
        println("Average response time: $avg ms")
        avg.shouldBeLessThan(50.0)
    }
})
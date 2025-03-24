package ru.cororo.corasense.test

import ru.cororo.corasense.model.advertiser.data.Advertiser
import ru.cororo.corasense.model.campaign.data.Campaign
import ru.cororo.corasense.model.campaign.dto.CampaignCreateRequest
import ru.cororo.corasense.model.client.data.Client
import java.util.UUID

val mockedClients = listOf(
    Client(UUID.fromString("550e8400-e29b-41d4-a716-446655440000"), "james123", 34, "New York", Client.Gender.MALE),
    Client(UUID.fromString("550e8400-e29b-41d4-a716-446655440001"), "mary_smith", 28, "Los Angeles", Client.Gender.FEMALE),
    Client(UUID.fromString("550e8400-e29b-41d4-a716-446655440002"), "john_doe", 45, "Chicago", Client.Gender.MALE),
    Client(UUID.fromString("550e8400-e29b-41d4-a716-446655440003"), "patricia88", 31, "Houston", Client.Gender.FEMALE),
    Client(UUID.fromString("550e8400-e29b-41d4-a716-446655440004"), "robert99", 29, "Phoenix", Client.Gender.MALE),
    Client(UUID.fromString("550e8400-e29b-41d4-a716-446655440005"), "jennifer_w", 37, "Philadelphia", Client.Gender.FEMALE),
    Client(UUID.fromString("550e8400-e29b-41d4-a716-446655440006"), "michael_b", 41, "San Antonio", Client.Gender.MALE),
    Client(UUID.fromString("550e8400-e29b-41d4-a716-446655440007"), "linda_m", 26, "San Diego", Client.Gender.FEMALE),
    Client(UUID.fromString("550e8400-e29b-41d4-a716-446655440008"), "william_t", 33, "Dallas", Client.Gender.MALE),
    Client(UUID.fromString("550e8400-e29b-41d4-a716-446655440009"), "elizabeth_k", 39, "San Jose", Client.Gender.FEMALE),
    Client(UUID.fromString("550e8400-e29b-41d4-a716-44665544000A"), "david_l", 48, "Austin", Client.Gender.MALE),
    Client(UUID.fromString("550e8400-e29b-41d4-a716-44665544000B"), "barbara_c", 30, "Jacksonville", Client.Gender.FEMALE),
    Client(UUID.fromString("550e8400-e29b-41d4-a716-44665544000C"), "richard_p", 36, "San Francisco", Client.Gender.MALE),
    Client(UUID.fromString("550e8400-e29b-41d4-a716-44665544000D"), "susan_h", 27, "Columbus", Client.Gender.FEMALE),
    Client(UUID.fromString("550e8400-e29b-41d4-a716-44665544000E"), "joseph_m", 44, "Fort Worth", Client.Gender.MALE),
    Client(UUID.fromString("550e8400-e29b-41d4-a716-44665544000F"), "jessica_r", 32, "Indianapolis", Client.Gender.FEMALE),
    Client(UUID.fromString("550e8400-e29b-41d4-a716-446655440010"), "thomas_v", 29, "Charlotte", Client.Gender.MALE),
    Client(UUID.fromString("550e8400-e29b-41d4-a716-446655440011"), "sarah_d", 40, "Seattle", Client.Gender.FEMALE),
    Client(UUID.fromString("550e8400-e29b-41d4-a716-446655440012"), "charles_s", 35, "Denver", Client.Gender.MALE),
    Client(UUID.fromString("550e8400-e29b-41d4-a716-446655440013"), "karen_m", 31, "Washington", Client.Gender.FEMALE)
).associateBy { it.id }

val mockedAdvertisers = listOf(
    Advertiser(UUID.fromString("550e8400-e29b-41d4-a716-446655440100"), "TechCorp"),
    Advertiser(UUID.fromString("550e8400-e29b-41d4-a716-446655440101"), "AdVantage"),
    Advertiser(UUID.fromString("550e8400-e29b-41d4-a716-446655440102"), "MarketBoost"),
    Advertiser(UUID.fromString("550e8400-e29b-41d4-a716-446655440103"), "BrandSphere"),
    Advertiser(UUID.fromString("550e8400-e29b-41d4-a716-446655440104"), "ClickFlow"),
    Advertiser(UUID.fromString("550e8400-e29b-41d4-a716-446655440105"), "SalesPro"),
    Advertiser(UUID.fromString("550e8400-e29b-41d4-a716-446655440106"), "PromoPeak"),
    Advertiser(UUID.fromString("550e8400-e29b-41d4-a716-446655440107"), "AdNova"),
    Advertiser(UUID.fromString("550e8400-e29b-41d4-a716-446655440108"), "MarketMinds"),
    Advertiser(UUID.fromString("550e8400-e29b-41d4-a716-446655440109"), "BoostAds"),
    Advertiser(UUID.fromString("550e8400-e29b-41d4-a716-44665544010A"), "AdRocket"),
    Advertiser(UUID.fromString("550e8400-e29b-41d4-a716-44665544010B"), "BrandElevate"),
    Advertiser(UUID.fromString("550e8400-e29b-41d4-a716-44665544010C"), "HyperAds"),
    Advertiser(UUID.fromString("550e8400-e29b-41d4-a716-44665544010D"), "BuzzMedia"),
    Advertiser(UUID.fromString("550e8400-e29b-41d4-a716-44665544010E"), "SmartReach"),
    Advertiser(UUID.fromString("550e8400-e29b-41d4-a716-44665544010F"), "AdStorm"),
    Advertiser(UUID.fromString("550e8400-e29b-41d4-a716-446655440110"), "PromoLab"),
    Advertiser(UUID.fromString("550e8400-e29b-41d4-a716-446655440111"), "BrandFusion"),
    Advertiser(UUID.fromString("550e8400-e29b-41d4-a716-446655440112"), "TargetPro"),
    Advertiser(UUID.fromString("550e8400-e29b-41d4-a716-446655440113"), "AdSphere")
).associateBy { it.id }

val testCampaignRequests = listOf(
    CampaignCreateRequest(
        100000,
        5000,
        0.02,
        0.50,
        "Boost Your Business!",
        "Join thousands of successful businesses with TechCorp marketing solutions.",
        5,
        30,
        targeting = CampaignCreateRequest.Targeting(
            Campaign.Targeting.Gender.ALL,
            25,
            50,
            "New York"
        )
    ),
    CampaignCreateRequest(
        50000,
        2000,
        0.03,
        0.60,
        "AdVantage: Ads That Work",
        "Maximize your reach with our cutting-edge advertising technology.",
        3,
        25,
        targeting = CampaignCreateRequest.Targeting(
            Campaign.Targeting.Gender.FEMALE,
            18,
            35,
            "Los Angeles"
        )
    ),
    CampaignCreateRequest(
        75000,
        3000,
        0.025,
        0.55,
        "MarketBoost - Elevate Your Brand",
        "Take your brand to the next level with MarketBoost advertising.",
        2,
        20,
        targeting = CampaignCreateRequest.Targeting(
            Campaign.Targeting.Gender.MALE,
            30,
            60,
            "Chicago"
        )
    ),
    CampaignCreateRequest(
        120000,
        7000,
        0.018,
        0.45,
        "BrandSphere - Your Audience Awaits",
        "Expand your audience with hyper-targeted digital advertising.",
        1,
        15,
        targeting = CampaignCreateRequest.Targeting(
            Campaign.Targeting.Gender.ALL,
            20,
            45,
            "Houston"
        )
    ),
    CampaignCreateRequest(
        90000,
        4000,
        0.022,
        0.52,
        "ClickFlow - More Clicks, More Sales",
        "Turn views into revenue with our AI-powered ad placements.",
        4,
        28,
        targeting = CampaignCreateRequest.Targeting(
            Campaign.Targeting.Gender.ALL,
            22,
            50,
            "San Francisco"
        )
    )
)

val actionAdvertisers = listOf(
    Advertiser(
        UUID.randomUUID(),
        "ТБанк"
    ),
    Advertiser(
        UUID.randomUUID(),
        "ВТБ"
    ),
    Advertiser(
        UUID.randomUUID(),
        "No-Code Team"
    ),
    Advertiser(
        UUID.randomUUID(),
        "OOO PROD"
    ),
    Advertiser(
        UUID.randomUUID(),
        "OOO DANO"
    )
)

val actionClients = listOf(
    Client(
        UUID.randomUUID(),
        "Cororo",
        17,
        "Moscow, Russia",
        Client.Gender.MALE
    ),
    Client(
        UUID.randomUUID(),
        "neruxov",
        15,
        "Podval, Russia",
        Client.Gender.FEMALE
    ),
    Client(
        UUID.randomUUID(),
        "CodeFlusher",
        17,
        "Vologda, Russia",
        Client.Gender.MALE
    )
)

val actionCampaignRequests = listOf(
    CampaignCreateRequest(
        1000,
        1000,
        1.0,
        2.0,
        "Йоу!!!!!",
        "Крутая реклама 1",
        1,
        10,
        targeting = CampaignCreateRequest.Targeting()
    ),
    CampaignCreateRequest(
        1000,
        1000,
        10.0,
        20000.0,
        "Йоу!!!!!",
        "Крутая реклама 2",
        1,
        10,
        targeting = CampaignCreateRequest.Targeting(
            gender = Campaign.Targeting.Gender.MALE
        )
    ),
    CampaignCreateRequest(
        1000,
        1000,
        10.0,
        200.0,
        "Йоу!!!!!",
        "Крутая реклама 3",
        1,
        10,
        targeting = CampaignCreateRequest.Targeting(
            gender = Campaign.Targeting.Gender.MALE
        )
    ),
    CampaignCreateRequest(
        1000,
        1000,
        15.0,
        205.0,
        "Йоу!!!!!",
        "Крутая реклама 4",
        1,
        10,
        targeting = CampaignCreateRequest.Targeting(
            gender = Campaign.Targeting.Gender.MALE
        )
    ),
    CampaignCreateRequest(
        1000,
        1000,
        15.0,
        205.0,
        "Йоу!!!!!",
        "Крутая реклама 5",
        1,
        10,
        targeting = CampaignCreateRequest.Targeting(
            gender = Campaign.Targeting.Gender.MALE
        )
    ),
    CampaignCreateRequest(
        1000,
        1000,
        15.0,
        205.0,
        "Йоу!!!!!",
        "Крутая реклама 6",
        1,
        10,
        targeting = CampaignCreateRequest.Targeting(
            gender = Campaign.Targeting.Gender.MALE
        )
    ),
    CampaignCreateRequest(
        1000,
        1000,
        15.0,
        205.0,
        "Йоу!!!!!",
        "Крутая реклама 7",
        1,
        10,
        targeting = CampaignCreateRequest.Targeting(
            gender = Campaign.Targeting.Gender.MALE
        )
    ),
)

val targetingTestCampaignRequests = listOf(
    CampaignCreateRequest(
        100,
        100,
        1.0,
        2.0,
        "Йоу!!!!!",
        "Крутая реклама 1",
        1,
        10,
        targeting = CampaignCreateRequest.Targeting()
    ),
    CampaignCreateRequest(
        100,
        100,
        1.0,
        2.0,
        "Йоу!!!!!",
        "Крутая реклама 2",
        1,
        10,
        targeting = CampaignCreateRequest.Targeting(
            gender = Campaign.Targeting.Gender.FEMALE
        )
    ),
    CampaignCreateRequest(
        100,
        100,
        1.0,
        2.0,
        "Йоу!!!!!",
        "Крутая реклама 3",
        1,
        10,
        targeting = CampaignCreateRequest.Targeting(
            gender = Campaign.Targeting.Gender.MALE
        )
    ),
    CampaignCreateRequest(
        100,
        100,
        1.0,
        2.0,
        "Йоу!!!!!",
        "Крутая реклама 4",
        1,
        10,
        targeting = CampaignCreateRequest.Targeting(
            gender = Campaign.Targeting.Gender.MALE,
            location = "Podval, Russia"
        )
    ),
    CampaignCreateRequest(
        100,
        100,
        1.0,
        2.0,
        "Йоу!!!!!",
        "Крутая реклама 5",
        1,
        10,
        targeting = CampaignCreateRequest.Targeting(
            gender = Campaign.Targeting.Gender.FEMALE,
            location = "Podval, Russia",
            ageFrom = 16
        )
    ),
    CampaignCreateRequest(
        100,
        100,
        1.0,
        2.0,
        "Йоу!!!!!",
        "Крутая реклама 6",
        1,
        10,
        targeting = CampaignCreateRequest.Targeting(
            gender = Campaign.Targeting.Gender.FEMALE,
            location = "Podval, Russia",
            ageFrom = 15
        )
    ),
    CampaignCreateRequest(
        100,
        100,
        1.0,
        2.0,
        "Йоу!!!!!",
        "Крутая реклама 7",
        1,
        10,
        targeting = CampaignCreateRequest.Targeting(
            gender = Campaign.Targeting.Gender.ALL,
            location = "Podval, Russia",
            ageTo = 14
        )
    ),
)

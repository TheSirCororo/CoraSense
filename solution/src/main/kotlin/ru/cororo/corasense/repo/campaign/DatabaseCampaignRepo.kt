package ru.cororo.corasense.repo.campaign

import io.ktor.server.application.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.plus
import org.jetbrains.exposed.sql.SqlExpressionBuilder.times
import org.jetbrains.exposed.sql.json.extract
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import ru.cororo.corasense.model.action.data.AdAction
import ru.cororo.corasense.model.campaign.data.Campaign
import ru.cororo.corasense.model.campaign.dto.CampaignCreateRequest
import ru.cororo.corasense.model.client.data.Client
import ru.cororo.corasense.plugin.sql
import ru.cororo.corasense.repo.action.AdActionTable
import ru.cororo.corasense.repo.ml.MLScoreTable
import ru.cororo.corasense.util.*
import java.util.*

object DatabaseCampaignRepo : CampaignRepo, KoinComponent {
    private const val CTR = 0.1
    private const val COST_WEIGHT = 1.0
    private const val ML_WEIGHT = 0.6
    private const val LIMIT_WEIGHT = 0.5

    private val application by inject<Application>()

    override suspend fun Campaign.getId(): UUID = id

    override suspend fun save(entity: Campaign): Unit = sql {
        CampaignTable.update(where = { CampaignTable.id eq entity.id }) {
            it[advertiserId] = entity.advertiserId
            it[impressionsLimit] = entity.impressionsLimit
            it[clicksLimit] = entity.clicksLimit
            it[costPerImpression] = entity.costPerImpression
            it[costPerClick] = entity.costPerClick
            it[adTitle] = entity.adTitle
            it[adText] = entity.adText
            it[startDate] = entity.startDate
            it[endDate] = entity.endDate
            it[targeting] = entity.targeting
            it[imageId] = entity.imageId
        }
    }

    override suspend fun createCampaign(advertiserId: UUID, request: CampaignCreateRequest): Campaign = sql {
        CampaignTable.insert {
            it[this.advertiserId] = advertiserId
            it[impressionsLimit] = request.impressionsLimit
            it[clicksLimit] = request.clicksLimit
            it[costPerImpression] = request.costPerImpression
            it[costPerClick] = request.costPerClick
            it[adTitle] = request.adTitle
            it[adText] = request.adText
            it[startDate] = request.startDate
            it[endDate] = request.endDate
            it[imageId] = request.imageId
            it[targeting] = request.targeting.let {
                Campaign.Targeting(
                    it?.gender, it?.ageFrom, it?.ageTo, it?.location
                )
            }
        }.let {
            with(CampaignTable) {
                Campaign(
                    it[id].value,
                    it[this.advertiserId].value,
                    it[impressionsLimit],
                    it[clicksLimit],
                    it[costPerImpression],
                    it[costPerClick],
                    it[adTitle],
                    it[adText],
                    it[startDate],
                    it[endDate],
                    it[imageId]?.value,
                    it[targeting]
                )
            }
        }
    }

    override suspend fun saveAll(entities: Iterable<Campaign>) = sql {
        entities.forEach { save(it) }
    }

    override suspend fun delete(id: UUID): Unit = sql {
        CampaignTable.deleteById(id)
    }

    override suspend fun get(id: UUID): Campaign? = sql {
        CampaignTable.getById(id)?.let {
            toCampaign(it)
        }
    }

    private fun toCampaign(result: ResultRow) = with(CampaignTable) {
        Campaign(
            result[this.id].value,
            result[advertiserId].value,
            result[impressionsLimit],
            result[clicksLimit],
            result[costPerImpression],
            result[costPerClick],
            result[adTitle],
            result[adText],
            result[startDate],
            result[endDate],
            result[imageId]?.value,
            result[targeting]
        )
    }

    override suspend fun getAdvertiserCampaigns(
        advertiserId: UUID,
        offset: Long,
        limit: Int
    ): Pair<Set<Campaign>, Long> = sql {
        var totalCount = CampaignTable.id.count().over().alias("total_count")
        var countValue: Long? = null
        CampaignTable.select(totalCount, CampaignTable).where { CampaignTable.advertiserId eq advertiserId }
            .offset(offset).limit(limit)
            .mapTo(mutableSetOf()) {
                countValue = it.getOrNull(totalCount)
                toCampaign(it)
            } to (countValue ?: 0)
    }

    override suspend fun getRelevantCampaignForClient(client: Client, day: Int): Campaign? = sql {
        // Расчёт наиболее подходящего для показа объявления
        val count = AdActionTable.id.count().over().partitionBy(AdActionTable.campaignId).alias("ad_views_count")
        val impressionsCountSubquery = AdActionTable.select(count, AdActionTable.campaignId)
            .where { AdActionTable.type eq AdAction.Type.IMPRESSION }
            .groupBy(AdActionTable.campaignId, AdActionTable.id)
            .alias("impressions_count_query")
        val impressionsCount = Expression.build {
            coalesce(impressionsCountSubquery[count], longLiteral(0))
        }

        val clicksCountSubquery = AdActionTable.select(count, AdActionTable.campaignId)
            .where { AdActionTable.type eq AdAction.Type.CLICK }
            .groupBy(AdActionTable.campaignId, AdActionTable.id)
            .alias("clicks_count_query")
        val clicksCount = Expression.build {
            coalesce(clicksCountSubquery[count], longLiteral(0))
        }

        val impressionsLimit = Expression.build {
            least(
                CampaignTable.impressionsLimit.castTo(DoubleColumnType()), CampaignTable.clicksLimit.castTo(
                    DoubleColumnType()
                ) / doubleLiteral(CTR)
            )
        }

        val ageFromOp =
            Op.build {
                coalesce(
                    CampaignTable.targeting.extract<Int>("age_from").castTo(IntegerColumnType()),
                    intLiteral(0)
                ) lessEq client.age
            }

        val ageToOp =
            Op.build {
                coalesce(
                    CampaignTable.targeting.extract<Int>("age_to").castTo(IntegerColumnType()),
                    intLiteral(200)
                ) greaterEq client.age
            }

        val locationOp = Op.build {
            coalesce(
                CampaignTable.targeting.extract<String>("location"),
                stringLiteral(client.location)
            ) eq client.location
        }

        val genderOp = Op.build {
            coalesce(
                CampaignTable.targeting.extract<String>("gender"),
                stringLiteral(client.gender.name)
            ) inList listOf(client.gender.name, "ALL")
        }

        val isActiveOp = Op.build { (CampaignTable.startDate lessEq day) and (CampaignTable.endDate greaterEq day) }

        val actionExistsLiteral = booleanLiteral(true).alias("action_exists_literal")
        val clientActionSubquery = AdActionTable.select(AdActionTable.campaignId, actionExistsLiteral)
            .where { (AdActionTable.clientId eq client.id) and (AdActionTable.type eq AdAction.Type.IMPRESSION) }
            .groupBy(AdActionTable.campaignId)
            .alias("action_exists_query")
        val actionExistsExpression = Expression.build {
            coalesce(clientActionSubquery[actionExistsLiteral], booleanLiteral(false))
        }.alias("action_exists")

        val limitOp = Op.build {
            actionExistsExpression.delegate or
                    ((impressionsCount.castTo(DoubleColumnType()) less impressionsLimit)
                            and (clicksCount less CampaignTable.clicksLimit.castTo(LongColumnType())))

        }

        fun ExpressionWithColumnType<Double>.normalize() = this
        fun ExpressionWithColumnType<Int>.normalize() = castTo(DoubleColumnType())

        // Если нужно, меняем на логарифмическую нормализацию
//        fun ExpressionWithColumnType<Double>.normalize() = (this + doubleLiteral(1.0)).log()
//        fun ExpressionWithColumnType<Int>.normalize() = (castTo(DoubleColumnType()) + doubleLiteral(1.0)).log()

        // Если нужно, меняем на степенную нормализацию (чем меньше степень - тем меньше разброс значений ==> больше прибыль возможно)
//        fun ExpressionWithColumnType<Double>.normalize() = power(doubleLiteral(0.3))
//        fun ExpressionWithColumnType<Int>.normalize() = castTo(DoubleColumnType()).power(doubleLiteral(0.3))

        val cost =
            CampaignTable.costPerClick * doubleLiteral(CTR) + CampaignTable.costPerImpression // Цена за клик менее значимая, потому что не все будут кликать по рекламе
        val costNormalization = cost.normalize()
        val minCostExpression = Expression.build {
            coalesce(costNormalization.min(), doubleLiteral(0.0))
        }.alias("min_cost")
        val maxCostExpression = Expression.build {
            coalesce(costNormalization.max(), doubleLiteral(0.0))
        }.alias("max_cost")

        val mlScore = MLScoreTable.score
        val mlScoreNormalization = mlScore.normalize()
        val minMlScoreExpression = Expression.build {
            coalesce(mlScoreNormalization.min(), doubleLiteral(0.0))
        }.alias("min_ml_score")
        val maxMlScoreExpression = Expression.build {
            coalesce(mlScoreNormalization.max(), doubleLiteral(0.0))
        }.alias("max_ml_score")

        val (maxMlScore, minMlScore, maxCost, minCost) = CampaignTable
            .leftJoin(MLScoreTable) { MLScoreTable.advertiserId eq CampaignTable.advertiserId }
            .leftJoin(clientActionSubquery) { CampaignTable.id eq clientActionSubquery[AdActionTable.campaignId] }
            .leftJoin(impressionsCountSubquery) { CampaignTable.id eq impressionsCountSubquery[AdActionTable.campaignId] }
            .leftJoin(clicksCountSubquery) { CampaignTable.id eq clicksCountSubquery[AdActionTable.campaignId] }
            .select(
                minCostExpression,
                maxCostExpression,
                minMlScoreExpression,
                maxMlScoreExpression
            )
            .where {
                isActiveOp and ageFromOp and ageToOp and locationOp and genderOp and limitOp
            }
            .singleOrNull()?.let {
                NormsData(
                    it[maxMlScoreExpression],
                    it[minMlScoreExpression],
                    it[maxCostExpression],
                    it[minCostExpression]
                )
            } ?: NormsData(0.0, 0.0, 0.0, 0.0)

        val limitScore = Expression.build {
            doubleLiteral(LIMIT_WEIGHT) * (doubleLiteral(1.0) - (impressionsCount.castTo(DoubleColumnType()) /
                    (impressionsLimit.castTo(DoubleColumnType()) + doubleLiteral(1.0))))
        }

        val costNormalized = Expression.build {
            doubleLiteral(COST_WEIGHT) * coalesce(
                (costNormalization - doubleLiteral(minCost)).castTo(DoubleColumnType()) / nullIf(
                    (doubleLiteral(maxCost) - doubleLiteral(minCost)).castTo(DoubleColumnType()),
                    doubleLiteral(0.0).castTo(DoubleColumnType())
                ).castTo(DoubleColumnType()), doubleLiteral(0.0)
            )
        }

        val mlScoreNormalized = Expression.build {
            doubleLiteral(ML_WEIGHT) * coalesce(
                (mlScoreNormalization - doubleLiteral(minMlScore)).castTo(DoubleColumnType()) / nullIf(
                    (doubleLiteral(maxMlScore) - doubleLiteral(minMlScore)).castTo(DoubleColumnType()),
                    doubleLiteral(0.0).castTo(DoubleColumnType())
                ).castTo(DoubleColumnType()), doubleLiteral(0.0)
            )
        }

        val campaignScore = Expression.build {
            costNormalized + mlScoreNormalized + limitScore
        }.alias("campaign_score")

        CampaignTable
            .leftJoin(MLScoreTable) { (CampaignTable.advertiserId eq MLScoreTable.advertiserId) and (MLScoreTable.clientId eq client.id) }
            .leftJoin(impressionsCountSubquery) { CampaignTable.id eq impressionsCountSubquery[AdActionTable.campaignId] }
            .leftJoin(clientActionSubquery) { CampaignTable.id eq clientActionSubquery[AdActionTable.campaignId] }
            .leftJoin(clicksCountSubquery) { CampaignTable.id eq clicksCountSubquery[AdActionTable.campaignId] }
            .select(
                campaignScore,
                CampaignTable,
                additionalExpressions = listOf(
                    actionExistsExpression,
                    impressionsCount,
                    clicksCount,
                    costNormalized,
                    mlScoreNormalized,
                    limitScore
                )
            )
            .where {
                isActiveOp and ageFromOp and ageToOp and locationOp and genderOp and limitOp
            }
            .groupBy(
                CampaignTable.id,
                MLScoreTable.score,
                actionExistsExpression,
                impressionsCount,
                clicksCount
            )
            .orderBy(actionExistsExpression to SortOrder.ASC, campaignScore to SortOrder.DESC, cost to SortOrder.DESC)
            .toList().let { rows ->
                rows.firstOrNull()?.let {
                    application.log.debug("Score of ${it[CampaignTable.adTitle]} is ${it[campaignScore]}")
                    toCampaign(it)
                }
            }
    }

    override suspend fun getAll(): List<Campaign> = sql {
        CampaignTable.selectAll().map { toCampaign(it) }
    }

    private data class NormsData(
        val maxMLScore: Double,
        val minMLScore: Double,
        val maxCost: Double,
        val minCost: Double
    )
}
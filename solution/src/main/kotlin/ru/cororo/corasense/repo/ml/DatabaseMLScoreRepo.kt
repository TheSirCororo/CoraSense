package ru.cororo.corasense.repo.ml

import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.upsert
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll
import ru.cororo.corasense.model.ml.data.MLScore
import ru.cororo.corasense.plugin.sql
import java.util.*

object DatabaseMLScoreRepo : MLScoreRepo {
    override suspend fun MLScore.getId(): Pair<UUID, UUID> =
        advertiserId to clientId

    override suspend fun save(entity: MLScore): Unit = sql {
        MLScoreTable.upsert(MLScoreTable.advertiserId, MLScoreTable.clientId, onUpdate = {
            it[MLScoreTable.score] = entity.score
        }) {
            with(MLScoreTable) {
                it[advertiserId] = entity.advertiserId
                it[clientId] = entity.clientId
                it[score] = entity.score
            }
        }
    }

    override suspend fun saveAll(entities: Iterable<MLScore>) = sql {
        entities.forEach { save(it) }
    }

    override suspend fun delete(id: Pair<UUID, UUID>): Unit = sql {
        MLScoreTable.deleteWhere { (advertiserId eq id.first) and (clientId eq id.second) }
    }

    override suspend fun get(id: Pair<UUID, UUID>): MLScore? = sql {
        MLScoreTable.selectAll().where { (MLScoreTable.advertiserId eq id.first) and (MLScoreTable.clientId eq id.second) }.singleOrNull()?.let {
            MLScore(it[MLScoreTable.advertiserId].value, it[MLScoreTable.clientId].value, it[MLScoreTable.score])
        }
    }
}
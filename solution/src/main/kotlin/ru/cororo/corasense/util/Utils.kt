package ru.cororo.corasense.util

import io.ktor.server.application.*
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.ColumnSet
import org.jetbrains.exposed.sql.Expression
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.selectAll
import ru.cororo.corasense.model.dto.Errors
import ru.cororo.corasense.model.dto.respond
import java.util.*

fun ApplicationCall.getUuid(stringId: String): UUID =
    try {
        UUID.fromString(stringId)
    } catch (_: Exception) {
        Errors.BadRequest.respond()
    }

fun <T : Any> IdTable<T>.deleteById(id: T) = deleteWhere { this.id eq id }

fun <T : Any> IdTable<T>.getById(id: T) = selectAll().where { this@getById.id eq id }.singleOrNull()

fun ColumnSet.select(
    expression: Expression<*>,
    vararg tables: Table,
    additionalExpressions: List<Expression<*>> = listOf()
) =
    select(tables.flatMap { it.columns } + expression + additionalExpressions)



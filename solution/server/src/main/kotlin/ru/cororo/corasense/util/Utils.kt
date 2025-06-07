package ru.cororo.corasense.util

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.jetbrains.exposed.v1.core.ColumnSet
import org.jetbrains.exposed.v1.core.Expression
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.eq
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.dao.id.IdTable
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll
import ru.cororo.corasense.model.dto.Errors
import ru.cororo.corasense.model.dto.respond
import java.io.ByteArrayOutputStream
import java.util.*

fun parseUuid(stringId: String): UUID =
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

fun ByteArray.asBytesFlow() = flow {
    emit(this@asBytesFlow)
}

suspend fun Flow<ByteArray>.readByteArray(): ByteArray {
    val output = ByteArrayOutputStream()
    collect { output.write(it) }
    return output.toByteArray()
}

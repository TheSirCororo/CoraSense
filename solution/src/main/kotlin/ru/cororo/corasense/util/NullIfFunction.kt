package ru.cororo.corasense.util

import org.jetbrains.exposed.sql.ColumnType
import org.jetbrains.exposed.sql.CustomFunction
import org.jetbrains.exposed.sql.DoubleColumnType
import org.jetbrains.exposed.sql.Expression

class NullIfFunction<T : Any?>(
    firstExpression: Expression<T>,
    secondExpression: Expression<T>,
    columnType: ColumnType<T & Any>
) : CustomFunction<T?>(
    "nullif",
    columnType,
    firstExpression,
    secondExpression
)

fun <T : Any?> nullIf(
    firstExpression: Expression<T>,
    secondExpression: Expression<T>,
    columnType: ColumnType<T & Any>
) =
    NullIfFunction(firstExpression, secondExpression, columnType)

fun nullIf(firstExpression: Expression<Double?>, secondExpression: Expression<Double?>) =
    nullIf(firstExpression, secondExpression, DoubleColumnType())

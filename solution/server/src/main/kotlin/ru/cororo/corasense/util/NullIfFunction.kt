package ru.cororo.corasense.util

import org.jetbrains.exposed.v1.core.ColumnType
import org.jetbrains.exposed.v1.core.CustomFunction
import org.jetbrains.exposed.v1.core.DoubleColumnType
import org.jetbrains.exposed.v1.core.Expression

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

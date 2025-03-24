package ru.cororo.corasense.util

import org.jetbrains.exposed.sql.CustomFunction
import org.jetbrains.exposed.sql.DoubleColumnType
import org.jetbrains.exposed.sql.Expression

class LeastFunction(
    vararg expressions: Expression<Double>
) : CustomFunction<Double>(
    "least",
    DoubleColumnType(),
    *expressions
)

fun least(vararg expressions: Expression<Double>) = LeastFunction(*expressions)

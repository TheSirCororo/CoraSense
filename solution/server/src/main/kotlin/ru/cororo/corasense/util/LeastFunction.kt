package ru.cororo.corasense.util

import org.jetbrains.exposed.v1.core.CustomFunction
import org.jetbrains.exposed.v1.core.DoubleColumnType
import org.jetbrains.exposed.v1.core.Expression

class LeastFunction(
    vararg expressions: Expression<Double>
) : CustomFunction<Double>(
    "least",
    DoubleColumnType(),
    *expressions
)

fun least(vararg expressions: Expression<Double>) = LeastFunction(*expressions)

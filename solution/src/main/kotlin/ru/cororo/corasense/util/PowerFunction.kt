package ru.cororo.corasense.util

import org.jetbrains.exposed.sql.CustomFunction
import org.jetbrains.exposed.sql.DoubleColumnType
import org.jetbrains.exposed.sql.Expression

class PowerFunction(
    numberExpression: Expression<Double>,
    powerExpression: Expression<Double>
) : CustomFunction<Double>(
    "power",
    DoubleColumnType(),
    numberExpression,
    powerExpression
)

fun Expression<Double>.power(another: Expression<Double>) = PowerFunction(this, another)

package ru.cororo.corasense.util

import org.jetbrains.exposed.v1.core.CustomFunction
import org.jetbrains.exposed.v1.core.DoubleColumnType
import org.jetbrains.exposed.v1.core.Expression

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

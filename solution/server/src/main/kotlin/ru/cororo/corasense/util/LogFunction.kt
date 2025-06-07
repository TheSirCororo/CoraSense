package ru.cororo.corasense.util

import org.jetbrains.exposed.v1.core.CustomFunction
import org.jetbrains.exposed.v1.core.DoubleColumnType
import org.jetbrains.exposed.v1.core.Expression

class LogFunction(
    numberExpression: Expression<Double>
) : CustomFunction<Double>(
    "log",
    DoubleColumnType(),
    numberExpression
)

fun Expression<Double>.log() = LogFunction(this)

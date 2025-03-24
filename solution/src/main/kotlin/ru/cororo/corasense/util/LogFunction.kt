package ru.cororo.corasense.util

import org.jetbrains.exposed.sql.CustomFunction
import org.jetbrains.exposed.sql.DoubleColumnType
import org.jetbrains.exposed.sql.Expression

class LogFunction(
    numberExpression: Expression<Double>
) : CustomFunction<Double>(
    "log",
    DoubleColumnType(),
    numberExpression
)

fun Expression<Double>.log() = LogFunction(this)

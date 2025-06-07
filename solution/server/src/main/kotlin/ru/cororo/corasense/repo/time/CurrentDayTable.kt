package ru.cororo.corasense.repo.time

import org.jetbrains.exposed.v1.core.Table

object CurrentDayTable : Table("current_day") {
    val currentDay = integer("current_day")
}
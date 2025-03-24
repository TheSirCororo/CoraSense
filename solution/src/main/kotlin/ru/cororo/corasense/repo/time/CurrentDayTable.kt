package ru.cororo.corasense.repo.time

import org.jetbrains.exposed.sql.Table

object CurrentDayTable : Table("current_day") {
    val currentDay = integer("current_day")
}
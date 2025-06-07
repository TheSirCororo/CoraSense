package ru.cororo.corasense.shared.util

data class PagedEntities<T>(val pageEntities: Set<T>, val totalSize: Long)

fun <T> Set<T>.paged(totalSize: Long) = PagedEntities(this, totalSize)

fun <T> emptyPaged() = PagedEntities<T>(setOf(), 0)

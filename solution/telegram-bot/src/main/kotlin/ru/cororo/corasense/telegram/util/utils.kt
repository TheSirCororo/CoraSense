package ru.cororo.corasense.telegram.util

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.koin.core.component.KoinComponent
import java.io.ByteArrayOutputStream

class ApiRoute : KoinComponent

suspend fun <R> telegramApi(configure: suspend ApiRoute.() -> R) = configure(ApiRoute())

fun ByteArray.asBytesFlow() = flow {
    emit(this@asBytesFlow)
}

suspend fun Flow<ByteArray>.readByteArray(): ByteArray {
    val output = ByteArrayOutputStream()
    collect { output.write(it) }
    return output.toByteArray()
}

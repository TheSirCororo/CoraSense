package ru.cororo.corasense.model.client.validator

import io.konform.validation.constraints.maxLength
import io.konform.validation.constraints.maximum
import io.konform.validation.constraints.minLength
import io.konform.validation.constraints.minimum
import ru.cororo.corasense.shared.model.client.Client
import ru.cororo.corasense.validation.validator

fun clientValidator() = validator<Client> {
    Client::login {
        minLength(3)
        maxLength(128)
    }

    Client::age {
        minimum(0)
        maximum(130)
    }

    Client::location {
        minLength(2)
        maxLength(128)
    }
}
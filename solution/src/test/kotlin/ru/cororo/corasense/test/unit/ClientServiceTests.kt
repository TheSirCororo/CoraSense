package ru.cororo.corasense.test.unit

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import ru.cororo.corasense.model.client.data.Client
import ru.cororo.corasense.model.client.data.Client.Gender
import ru.cororo.corasense.repo.client.ClientRepo
import ru.cororo.corasense.service.ClientService
import java.util.UUID

// unit тесты написаны при помощи чата гпт
class ClientServiceTests : StringSpec({
    val clientRepo = mockk<ClientRepo>()
    val clientService = ClientService(clientRepo)
    fun randomClient() = Client(
        id = UUID.randomUUID(),
        login = "test_user",
        age = 25,
        location = "Moscow",
        gender = Gender.MALE
    )

    "should return expected client by ID" {
        val client = randomClient()
        coEvery { clientRepo.get(client.id) } returns client

        val result = clientService.getClient(client.id)

        result shouldBe client
        coVerify(exactly = 1) { clientRepo.get(client.id) }
    }

    "should save client" {
        val client = randomClient()
        coEvery { clientRepo.save(client) } just Runs

        clientService.saveClient(client)

        coVerify(exactly = 1) { clientRepo.save(client) }
    }

    "should save multiple clients" {
        val clients = listOf(randomClient(), randomClient())
        coEvery { clientRepo.saveAll(clients) } just Runs

        clientService.saveClients(clients)

        coVerify(exactly = 1) { clientRepo.saveAll(clients) }
    }

    "should return clients by login" {
        val clients = setOf(randomClient().copy(login = "test_user"))
        coEvery { clientRepo.findByLogin("test_user") } returns clients

        val result = clientService.findClientsByLogin("test_user")

        result shouldBe clients
        coVerify(exactly = 1) { clientRepo.findByLogin("test_user") }
    }
})
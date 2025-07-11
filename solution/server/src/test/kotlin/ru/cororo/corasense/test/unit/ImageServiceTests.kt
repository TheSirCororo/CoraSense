package ru.cororo.corasense.test.unit

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.ktor.server.config.*
import io.ktor.server.testing.*
import io.ktor.test.dispatcher.*
import io.mockk.*
import ru.cororo.corasense.repo.image.ImageRepo
import ru.cororo.corasense.service.ImageServiceImpl
import ru.cororo.corasense.shared.model.image.Image
import ru.cororo.corasense.storage.FileImageStorageProvider
import ru.cororo.corasense.storage.ImageStorageProvider
import ru.cororo.corasense.storage.S3ImageStorageProvider
import ru.cororo.corasense.util.readByteArray
import java.util.*

// unit тесты написаны при помощи чата гпт
class ImageServiceTests : StringSpec({
    val mockRepo = mockk<ImageRepo>(relaxed = true)
    val mockFileStorage = mockk<FileImageStorageProvider>(relaxed = true)
    val mockS3Storage = mockk<S3ImageStorageProvider>(relaxed = true)
    val testId = UUID.randomUUID()
    val testImageName = "test_image.png"
    val testImage = Image(testId, testImageName)

    beforeTest {
        clearAllMocks()
    }

    fun runWithService(storageType: String, storage: ImageStorageProvider, block: ImageServiceImpl.() -> Unit) {
        testApplication {
            environment {
                config = MapApplicationConfig(
                    "image.upload_dir" to "test_images",
                    "image.storage_type" to storageType,
                    "image.s3_endpoint" to "http://localhost",
                    "image.s3_key_id" to "test-key",
                    "image.s3_key_value" to "test-secret",
                    "image.s3_bucket" to "test-bucket"
                )
            }

            application {
                ImageServiceImpl(mockRepo, this, storage).block()
            }
        }
    }

    "should save image metadata in database" {
        coEvery { mockRepo.createNewImage(testId, testImageName) } returns testImage

        runWithService("FILE", mockFileStorage) {
            testSuspend {
                saveImageData(testId, testImageName)
                coVerify { mockRepo.createNewImage(testId, testImageName) }
            }
        }
    }

    "should return image metadata from database" {
        coEvery { mockRepo.get(testId) } returns testImage

        runWithService("FILE", mockFileStorage) {
            testSuspend {
                val imageName = getImageData(testId)
                imageName shouldBe testImage
                coVerify { mockRepo.get(testId) }
            }
        }
    }

    "should delete image from storage and database (file)" {
        coEvery { mockFileStorage.deleteImage(testId) } just Runs
        coEvery { mockRepo.delete(testId) } just Runs

        runWithService("FILE", mockFileStorage) {
            testSuspend {
                deleteImage(testId)
                coVerify { mockFileStorage.deleteImage(testId) }
                coVerify { mockRepo.delete(testId) }
            }
        }
    }

    "should delete image from storage and database (s3)" {
        coEvery { mockS3Storage.deleteImage(testId) } just Runs
        coEvery { mockRepo.delete(testId) } just Runs

        runWithService("S3", mockS3Storage) {
            testSuspend {
                deleteImage(testId)
                coVerify { mockS3Storage.deleteImage(testId) }
                coVerify { mockRepo.delete(testId) }
            }
        }
    }

    "should load image bytes from storage (file)" {
        val imageData = byteArrayOf(1, 2, 3)
        coEvery { mockFileStorage.loadImage(testId) } returns imageData

        runWithService("FILE", mockFileStorage) {
            testSuspend {
                val bytes = loadImageBytes(testId)
                bytes.readByteArray() shouldBe imageData
                coVerify { mockFileStorage.loadImage(testId) }
            }
        }
    }

    "should load image bytes from storage (s3)" {
        val imageData = byteArrayOf(1, 2, 3)
        coEvery { mockS3Storage.loadImage(testId) } returns imageData

        runWithService("S3", mockS3Storage) {
            testSuspend {
                val bytes = loadImageBytes(testId)
                bytes.readByteArray() shouldBe imageData
                coVerify { mockS3Storage.loadImage(testId) }
            }
        }
    }
})

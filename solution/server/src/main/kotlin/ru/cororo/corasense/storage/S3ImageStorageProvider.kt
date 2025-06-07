package ru.cororo.corasense.storage

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import ru.cororo.corasense.service.ImageServiceImpl.Companion.allowedFileExtensions
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.http.apache.ApacheHttpClient
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import java.io.ByteArrayOutputStream
import java.net.URI
import java.util.*

class S3ImageStorageProvider(s3Endpoint: String, s3KeyId: String, s3KeyValue: String, private val s3Bucket: String) :
    ImageStorageProvider {
    private val s3Client = S3Client.builder()
        .endpointOverride(URI.create(s3Endpoint))
        .region(Region.EU_NORTH_1)
        .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(s3KeyId, s3KeyValue)))
        .httpClientBuilder(ApacheHttpClient.builder())
        .build()

    init {
        s3Client.listBuckets().buckets().find { it.name() == s3Bucket } ?: error("Бакет с именем $s3Bucket не найден!")
    }

    override suspend fun uploadImage(id: UUID, fileName: String, bytesFlow: Flow<ByteArray>): String? = withContext(Dispatchers.IO) {
        if (!allowedFileExtensions.any { fileName.endsWith(it) }) return@withContext null

        val resultOutput = ByteArrayOutputStream()
        bytesFlow.collect { resultOutput.write(it) }
        s3Client.putObject(
            PutObjectRequest.builder().bucket(s3Bucket).key(id.toString()).build(),
            RequestBody.fromBytes(resultOutput.toByteArray())
        )

        fileName
    }

    override suspend fun loadImage(id: UUID) = withContext(Dispatchers.IO) {
        s3Client.getObject(GetObjectRequest.builder().bucket(s3Bucket).key(id.toString()).build()).readBytes()
    }

    override suspend fun deleteImage(id: UUID) {
        s3Client.deleteObject(DeleteObjectRequest.builder().bucket(s3Bucket).bucket(id.toString()).build())
    }
}
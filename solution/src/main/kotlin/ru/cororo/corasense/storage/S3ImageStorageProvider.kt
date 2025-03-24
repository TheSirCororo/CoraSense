package ru.cororo.corasense.storage

import io.ktor.http.content.*
import io.ktor.utils.io.*
import ru.cororo.corasense.service.ImageService.Companion.allowedFileExtensions
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.http.apache.ApacheHttpClient
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.model.PutObjectRequest
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

    override suspend fun uploadImage(
        multiPartData: PartData.FileItem,
        id: UUID
    ): String? {
        val fileName = multiPartData.originalFileName ?: return null
        if (!allowedFileExtensions.any { fileName.endsWith(it) }) return null
        s3Client.putObject(
            PutObjectRequest.builder().bucket(s3Bucket).key(id.toString()).build(),
            RequestBody.fromBytes(multiPartData.provider().toByteArray())
        )

        return fileName
    }

    override suspend fun loadImage(id: UUID) =
        s3Client.getObject(GetObjectRequest.builder().bucket(s3Bucket).key(id.toString()).build()).readBytes()

    override suspend fun deleteImage(id: UUID) {
        s3Client.deleteObject(DeleteObjectRequest.builder().bucket(s3Bucket).bucket(id.toString()).build())
    }
}
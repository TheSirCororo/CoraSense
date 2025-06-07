package ru.cororo.corasense.route.image

import io.github.smiley4.ktoropenapi.resources.delete
import io.github.smiley4.ktoropenapi.resources.get
import io.github.smiley4.ktoropenapi.resources.post
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.utils.io.*
import org.koin.core.component.get
import ru.cororo.corasense.inject.api
import ru.cororo.corasense.model.dto.Errors
import ru.cororo.corasense.model.dto.StatusResponse
import ru.cororo.corasense.model.dto.respond
import ru.cororo.corasense.model.dto.respondOk
import ru.cororo.corasense.route.Paths
import ru.cororo.corasense.service.ImageServiceImpl
import ru.cororo.corasense.shared.model.image.Image
import ru.cororo.corasense.shared.service.ImageService
import ru.cororo.corasense.util.asBytesFlow
import ru.cororo.corasense.util.parseUuid
import ru.cororo.corasense.util.readByteArray
import java.util.*

fun Route.imageApi() = api {
    val imageService = it.get<ImageService>()

    post<Paths.Images>({
        summary = "Загрузить изображение."
        description = "Необходимо отправить файл в единственном поле multipart data. Поддерживаемые типы: ${
            ImageServiceImpl.allowedFileExtensions.joinToString(", ")
        }"

        request {
            multipartBody {}
        }

        response {
            code(HttpStatusCode.Created) {
                body<Image>()
            }
        }
    }) {
        if ((call.request.header(HttpHeaders.ContentLength)?.toLongOrNull()
                ?: Long.MAX_VALUE) > imageService.maxImageSize()
        ) {
            Errors.BadRequest.respond()
        }

        val multipart = call.receiveMultipart()
        val part = multipart.readPart() ?: Errors.BadRequest.respond()
        if (part !is PartData.FileItem) {
            Errors.BadRequest.respond()
        }

        val uploadFileName = part.originalFileName ?: Errors.BadRequest.respond()
        val id = UUID.randomUUID()
        val result = imageService.uploadImage(id, uploadFileName, part.provider().toByteArray().asBytesFlow())
            ?: Errors.BadRequest.respond()

        part.dispose()

        call.respond(status = HttpStatusCode.Created, imageService.saveImageData(id, result.resultFileName))
    }

    get<Paths.Images.ById>({
        summary = "Скачать изображение"

        request {
            queryParameter<Boolean>("download") {
                description =
                    "Скачивать ли изображение? От этого зависит тип Content-Disposition: attachment (download=true) или inline (download=false). download=false подойдёт для встраивания на сайты."
                required = false
            }

            pathParameter<UUID>("imageId") {
                description = "ID изображения"
                required = true
            }
        }

        response {
            code(HttpStatusCode.OK) {
                description =
                    "Изображение существует и успешно получено. Возвращается Content-Disposition (attachment или inline) с filename."
                header<String>("Content-Type: image/*")
            }

            code(HttpStatusCode.NotFound) {
                body<StatusResponse.Error> {
                    example("Image not found") {
                        value = Errors.ImageNotFound
                    }
                }
            }
        }
    }) {
        val id = parseUuid(it.imageId)
        val download = call.queryParameters["download"]?.toBooleanStrictOrNull() == true
        val image = imageService.getImageData(id) ?: Errors.ImageNotFound.respond()
        val bytes = imageService.loadImageBytes(id) ?: run {
            imageService.deleteImage(id)
            Errors.ImageNotFound.respond()
        }
        call.response.header(
            HttpHeaders.ContentDisposition,
            (if (download) ContentDisposition.Attachment else ContentDisposition.Inline).withParameter(
                ContentDisposition.Parameters.FileName,
                image.name
            ).toString()
        )

        call.respondBytes(bytes.readByteArray(), contentType = ContentType.Image.Any)
    }

    delete<Paths.Images.ById>({
        summary = "Удалить изображение"
        description =
            "Удаление изображения из БД и хранилища. Убедитесь, что ни в какой кампании это изображение не используется."

        request {
            pathParameter<UUID>("imageId") {
                required = true
                description = "ID изображения"
            }
        }

        response {
            code(HttpStatusCode.OK) {
                body<StatusResponse.Ok> {
                    example("OK") {
                        value = StatusResponse.Ok()
                    }
                }
            }

            code(HttpStatusCode.NotFound) {
                body<StatusResponse.Error> {
                    example("Image not found") {
                        value = Errors.ImageNotFound
                    }
                }
            }
        }
    }) {
        val id = parseUuid(it.imageId)
        imageService.getImageData(id) ?: Errors.ImageNotFound.respond()
        imageService.deleteImage(id)
        call.respondOk()
    }
}
package com.boclips.videos.service.presentation.converters

import arrow.core.Either
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.domain.service.video.VideoRepository
import com.boclips.videos.service.presentation.CustomMetadataValidated
import com.boclips.videos.service.presentation.CustomMetadataValidationResult
import com.boclips.videos.service.presentation.DataRowsContainMetadataErrors
import com.boclips.videos.service.presentation.MissingColumn
import com.boclips.videos.service.presentation.NotCsvMetadataFile
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import com.github.doyaaaaaken.kotlincsv.util.CSVFieldNumDifferentException
import org.bson.types.ObjectId
import org.springframework.core.io.InputStreamSource
import java.io.InputStream

class CustomMetadataFileValidator(val videoRepository: VideoRepository) {
    object Headers {
        const val TOPIC = "Topic"
        const val TOPIC_ID = "Topic ID"
        const val VIDEO_ID = "ID"
    }

    fun validate(input: InputStreamSource?): CustomMetadataValidationResult {
        val inputStream = input?.inputStream ?: throw RuntimeException("file is empty!")
        val parsedFile: Either<List<RawCustomMappingMetadata>, NotCsvMetadataFile> =
            readCsvFile(inputStream)

        return when (parsedFile) {
            is Either.Left -> validateMappings(parsedFile.value)
            is Either.Right -> parsedFile.value
        }
    }

    private fun validateMappings(items: List<RawCustomMappingMetadata>): CustomMetadataValidationResult {
        anyMissingHeaders(items.firstOrNull())?.let { missingHeader -> return missingHeader }

        val confirmedVideoIds =
            videoRepository.findAll(
                items.mapNotNull {
                    if (ObjectId.isValid(it.videoId)) {
                        VideoId(it.videoId!!)
                    } else {
                        null
                    }
                }.toList()
            ).map { it.videoId.value }

        val errors = items.mapIndexedNotNull { index, rawCustomMappingMetadata ->
            CustomMetadataMappingValidator.validateMapping(
                index = index,
                item = rawCustomMappingMetadata,
                confirmedVideoIds = confirmedVideoIds
            )
        }

        return if (errors.isEmpty()) {
            CustomMetadataValidated(
                entries = items.mapIndexedNotNull { index, it ->
                    it.validated(
                        index
                    )
                }
            )
        } else {
            DataRowsContainMetadataErrors(errors = errors)
        }
    }

    private fun anyMissingHeaders(header: RawCustomMappingMetadata?): MissingColumn? {
        if (header == null) {
            return null
        }

        if (header.topic == null) {
            return MissingColumn(Headers.TOPIC)
        }

        if (header.topicId == null) {
            return MissingColumn(Headers.TOPIC_ID)
        }

        if (header.videoId == null) {
            return MissingColumn(Headers.VIDEO_ID)
        }

        return null
    }

    private fun readCsvFile(inputStream: InputStream): Either<List<RawCustomMappingMetadata>, NotCsvMetadataFile> {
        return try {
            Either.Left(
                csvReader()
                    .readAllWithHeader(inputStream)
                    .map { toMetadata(it) }
            )
        } catch (e: CSVFieldNumDifferentException) {
            Either.Right(NotCsvMetadataFile)
        }
    }

    private fun toMetadata(row: Map<String, String>): RawCustomMappingMetadata {
        val videoId = row[Headers.VIDEO_ID]?.trim()
        val topic = row[Headers.TOPIC]?.trim()
        val topicId = row[Headers.TOPIC_ID]?.trim()
        return RawCustomMappingMetadata(topic = topic, topicId = topicId, videoId = videoId)
    }
}

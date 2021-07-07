package com.boclips.videos.service.presentation.converters

import arrow.core.Either
import com.boclips.videos.service.application.GetAllCategories
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.domain.service.TagRepository
import com.boclips.videos.service.domain.service.video.VideoRepository
import com.boclips.videos.service.presentation.CategoryValidationResult
import com.boclips.videos.service.presentation.CsvValidated
import com.boclips.videos.service.presentation.CsvValidatedWithEmptyIds
import com.boclips.videos.service.presentation.DataRowsContainErrors
import com.boclips.videos.service.presentation.NotCsvFile
import com.boclips.videos.service.presentation.VideoIdOrCategoryCodeColumnIsMissing
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import com.github.doyaaaaaken.kotlincsv.util.CSVFieldNumDifferentException
import org.bson.types.ObjectId
import org.springframework.core.io.InputStreamSource
import java.io.InputStream

class VideoTaggingCsvFileValidator(
    val getAllCategories: GetAllCategories,
    val videoRepository: VideoRepository,
    val tagRepository: TagRepository
) {
    fun validate(input: InputStreamSource?): CategoryValidationResult {
        val inputStream = input?.inputStream ?: throw RuntimeException("file is empty!")
        val parsedFile: Either<List<RawCategoryMappingMetadata>, NotCsvFile> = readCsvFile(inputStream)

        return when (parsedFile) {
            is Either.Left -> validateMappings(parsedFile.value)
            is Either.Right -> parsedFile.value
        }
    }

    private fun validateMappings(items: List<RawCategoryMappingMetadata>): CategoryValidationResult {
        if (categoryCodeOrVideoIdColumnIsMissing(items)) {
            return VideoIdOrCategoryCodeColumnIsMissing
        }

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

        val categoryCodes = getAllCategories().map { it.code.value }

        val tags = tagRepository.findAll().map { it.label }

        val errors = items.mapIndexedNotNull { index, item ->
            CategoryMappingValidator.validateMapping(index, item, categoryCodes, confirmedVideoIds, tags)
        }

        val validItems = items.mapIndexedNotNull { index, it -> it.validated(index) }

        val entriesWithoutIds = validItems.filter { it.videoId.isEmpty() }

        return if (errors.isEmpty() && entriesWithoutIds.isEmpty()) {
            CsvValidated(validItems)
        } else if (errors.isEmpty() && entriesWithoutIds.isNotEmpty()) {
            CsvValidatedWithEmptyIds(
                entriesWithIds = validItems.filter { it.videoId.isNotEmpty() },
                entriesWithoutIds = entriesWithoutIds
            )
        } else {
            DataRowsContainErrors(errors = errors)
        }
    }

    private fun categoryCodeOrVideoIdColumnIsMissing(items: List<RawCategoryMappingMetadata>) =
        items.firstOrNull()?.categoryCode == null || items.firstOrNull()?.videoId == null

    private fun readCsvFile(inputStream: InputStream): Either<List<RawCategoryMappingMetadata>, NotCsvFile> {
        return try {
            Either.Left(
                csvReader()
                    .readAllWithHeader(inputStream)
                    .map { toMetadata(it) }
            )
        } catch (e: CSVFieldNumDifferentException) {
            Either.Right(NotCsvFile)
        }
    }

    private fun toMetadata(row: Map<String, String>): RawCategoryMappingMetadata {
        val categoryCode = row["Category Code"]?.trim()
        val videoId = row["ID"]?.trim()
        val tag = row["Pedagogy Tag"]?.trim()
        return RawCategoryMappingMetadata(categoryCode = categoryCode, tag = tag, videoId = videoId)
    }
}

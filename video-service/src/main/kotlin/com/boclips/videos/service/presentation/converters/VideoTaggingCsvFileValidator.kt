package com.boclips.videos.service.presentation.converters

import arrow.core.Either
import com.boclips.videos.service.application.GetAllCategories
import com.boclips.videos.service.presentation.CategoriesValid
import com.boclips.videos.service.presentation.CategoryValidationResult
import com.boclips.videos.service.presentation.DataRowsContainErrors
import com.boclips.videos.service.presentation.NotCsvFile
import com.boclips.videos.service.presentation.VideoIdOrCategoryCodeColumnIsMissing
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import com.github.doyaaaaaken.kotlincsv.util.CSVFieldNumDifferentException
import org.springframework.core.io.InputStreamSource
import java.io.InputStream

class VideoTaggingCsvFileValidator(val getAllCategories: GetAllCategories) {
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

        val categoryCodes = getAllCategories().map { it.code.value }
        val errors = items.mapIndexedNotNull { index, item ->
            CategoryMappingValidator.validateMapping(index, item, categoryCodes)
        }

        return if (errors.isEmpty()) {
            CategoriesValid(items.map { it.validated() })
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
        return RawCategoryMappingMetadata(categoryCode = categoryCode, videoId = videoId)
    }
}

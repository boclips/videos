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
        return when (val parsedFile = readCsvFile(inputStream)) {
            is Either.Left -> validateMappings(parsedFile.value)
            is Either.Right -> parsedFile.value
        }
    }

    private fun validateMappings(items: List<CategoryMappingMetadata>): CategoryValidationResult {
        if (categoryCodeOrVideoIdColumnIsMissing(items)) {
            return VideoIdOrCategoryCodeColumnIsMissing
        }

        val categoryCodes = getAllCategories().map { it.code.value }
        val errors = items.mapIndexedNotNull { index, item ->
            CategoryMappingValidator.validateMapping(index, item, categoryCodes)
        }


        return if (errors.isEmpty()) {
            CategoriesValid(items.size)
        } else {
            DataRowsContainErrors(errors = errors)
        }
    }

    private fun categoryCodeOrVideoIdColumnIsMissing(items: List<CategoryMappingMetadata>) =
        items.firstOrNull()?.categoryCode == null || items.firstOrNull()?.videoId == null

    private fun readCsvFile(inputStream: InputStream): Either<List<CategoryMappingMetadata>, NotCsvFile> {
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

    private fun toMetadata(row: Map<String, String>): CategoryMappingMetadata {
        val categoryCode = row["Category Code"]
        val videoId = row["ID"]
        return CategoryMappingMetadata(categoryCode = categoryCode, videoId = videoId)
    }
}

package com.boclips.videos.service.presentation.converters

import arrow.core.Either
import com.boclips.videos.service.application.GetAllCategories
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import com.github.doyaaaaaken.kotlincsv.util.CSVFieldNumDifferentException
import org.springframework.core.io.InputStreamSource
import org.springframework.stereotype.Component
import java.io.InputStream

@Component
class VideoTaggingCsvFileValidator(val getAllCategories: GetAllCategories) {

    fun validate(input: InputStreamSource?): CategoryValidationResult {
        val inputStream = input?.inputStream
        if (inputStream == null) {
            throw RuntimeException("file is empty!")
        }
        val parsedFile = readCsvFile(inputStream)
        return when {
            parsedFile is Either.Left -> validateMappings(parsedFile.value)
            else -> (parsedFile as Either.Right).value
        }
    }

    private fun validateMappings(items: List<CategoryMappingMetadata>): CategoryValidationResult {
        if (categoryCodeOrVideoIdColumnIsMissing(items)) {
            return VideoIdOrCategoryCodeColumnIsMissing
        }

        val errors = emptyList<VideoTaggingValidationError>().toMutableList()
        val categoryCodes = getAllCategories().map { it.code.value }
        for ((index, item) in items.withIndex()) {
            CategoryMappingValidator.validateMapping(index, item, categoryCodes)?.let { errors.add(it) }
        }

        if (errors.isEmpty()) {
            return CategoriesValid(items.size)
        } else {
            return DataRowsContainErrors(errors = errors)
        }
    }

    private fun categoryCodeOrVideoIdColumnIsMissing(items: List<CategoryMappingMetadata>) =
        items.firstOrNull()?.categoryCode == null || items.firstOrNull()?.videoId == null

    private fun readCsvFile(inputStream: InputStream): Either<List<CategoryMappingMetadata>, NotCsvFile> {
        try {
            return Either.Left(
                csvReader()
                    .readAllWithHeader(inputStream)
                    .map { toMetadata(it) }
            )
        } catch (e: CSVFieldNumDifferentException) {
            return Either.Right(NotCsvFile)
        }
    }

    private fun toMetadata(row: Map<String, String>): CategoryMappingMetadata {
        val categoryCode = row["Category Code"]
        val videoId = row["ID"]
        return CategoryMappingMetadata(categoryCode = categoryCode, videoId = videoId)
    }
}

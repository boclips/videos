package com.boclips.videos.service.presentation.converters

import arrow.core.Either
import com.boclips.videos.service.application.GetAllCategories
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import com.github.doyaaaaaken.kotlincsv.util.CSVFieldNumDifferentException
import org.bson.types.ObjectId
import org.springframework.core.io.InputStreamSource
import org.springframework.stereotype.Component
import java.io.InputStream

@Component
class CategoryMappingValidator(val getAllCategories: GetAllCategories) {

    fun validate(input: InputStreamSource?): CategoryValidationResult {
        val inputStream = input?.inputStream
        if (inputStream == null) {
            throw RuntimeException("file is empty!")
        }
        val parsedFile = readCsvFile(inputStream)
        return when {
            parsedFile is Either.Left -> validateEntries(parsedFile.value)
            else -> (parsedFile as Either.Right).value
        }
    }

    private fun validateEntries(items: List<CategoryMappingMetadata>): CategoryValidationResult {
        val errors = emptyList<CategoryValidationError>().toMutableList()
        if (items.isEmpty()) {
            errors.add(InvalidFile)
        }
        if (items.first().categoryCode == null || items.first().videoId == null) {
            return InvalidColumns
        }

        val categoryCodes = getAllCategories().map { it.code.value }
        for ((index, item) in items.withIndex()) {
            validateEntry(index, item, categoryCodes)?.let { errors.add(it) }
        }

        if (errors.isEmpty()) {
            return CategoriesValid(items.size)
        } else {
            return CategoriesInvalid(errors = errors)
        }
    }

    private fun validateEntry(index: Int, item: CategoryMappingMetadata, categoryCodes: List<String>): CategoryValidationError? {
        if (item.videoId.isNullOrEmpty()) {
            return MissingVideoId(rowIndex = index)
        } else {
            try {
                ObjectId(item.videoId)
            } catch (e: IllegalArgumentException) {
                return InvalidVideoId(rowIndex = index, invalidId = item.videoId)
            }
        }

        if (!item.categoryCode.isNullOrEmpty() && !categoryCodes.contains(item.categoryCode)) {
            return InvalidCategoryCode(code = item.categoryCode, rowIndex = index)
        }

        return null
    }

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

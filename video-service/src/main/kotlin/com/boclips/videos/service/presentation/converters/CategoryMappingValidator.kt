package com.boclips.videos.service.presentation.converters

import arrow.core.Either
import com.boclips.videos.service.application.GetAllCategories
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvMappingException
import com.fasterxml.jackson.dataformat.csv.CsvParser
import com.fasterxml.jackson.dataformat.csv.CsvSchema
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.bson.types.ObjectId
import org.springframework.core.io.InputStreamSource
import org.springframework.stereotype.Component
import java.io.StringReader

@Component
class CategoryMappingValidator(val getAllCategories: GetAllCategories) {

    fun validate(input: InputStreamSource?): CategoryValidationResult {
        val bytes = input?.inputStream?.readBytes()
        if (bytes == null) {
            throw RuntimeException("file is empty!")
        }
        val entriesOrError = readCsvFile(bytes)
        return when {
            entriesOrError is Either.Left -> validateEntries(entriesOrError.value)
            else -> (entriesOrError as Either.Right).value
        }
    }

    private fun validateEntries(items: List<CategoryMappingMetadata>): CategoryValidationResult {
        val categoryCodes = getAllCategories().map { it.code.value }
        val errors = emptyList<CategoryValidationError>().toMutableList()
        for ((index, item) in items.withIndex()) {
            if (item.videoId.isNullOrEmpty()) {
                errors.add(MissingVideoId(rowIndex = index))
            } else {
                try {
                    ObjectId(item.videoId)
                } catch (e: IllegalArgumentException) {
                    errors.add(InvalidVideoId(rowIndex = index, invalidId = item.videoId))
                }
            }

            if (!item.categoryCode.isNullOrEmpty() && !categoryCodes.contains(item.categoryCode)) {
                errors.add(InvalidCategoryCode(code = item.categoryCode, rowIndex = index))
            }
        }

        if (items.isEmpty()) {
            errors.add(InvalidFile)
        }

        if (errors.isEmpty()) {
            return CategoriesValid(items.size)
        } else {
            return CategoriesInvalid(errors = errors)
        }
    }

    private fun readCsvFile(bytes: ByteArray): Either<List<CategoryMappingMetadata>, NotCsvFile> {
        try {
            StringReader(String(bytes)).use { reader ->
                val redValued = CsvMapper()
                    .configure(CsvParser.Feature.FAIL_ON_MISSING_COLUMNS, true)
                    .apply { registerModule(KotlinModule()) }
                    .readerFor(CategoryMappingMetadata::class.java)
                    .with(CsvSchema.emptySchema().withHeader().withLineSeparator(""))
                    .readValues<CategoryMappingMetadata>(reader)
                    .readAll()

                return Either.Left(redValued)
            }
        } catch (e: CsvMappingException) {
            return Either.Right(NotCsvFile)
        }
    }
}

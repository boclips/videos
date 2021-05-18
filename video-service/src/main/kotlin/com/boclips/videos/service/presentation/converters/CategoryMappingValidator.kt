package com.boclips.videos.service.presentation.converters

import com.boclips.videos.service.application.GetAllCategories
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvParser
import com.fasterxml.jackson.dataformat.csv.CsvSchema
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.bson.types.ObjectId
import org.springframework.core.io.InputStreamSource
import org.springframework.stereotype.Component
import java.io.StringReader

@Component
class CategoryMappingValidator(val getAllCategories: GetAllCategories) {

    fun validate(input: InputStreamSource): CategoryValidationResult =
        readCsvFile<CategoryMappingMetadata>(input.inputStream.readBytes()).let { items ->
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
                return@let CategoriesValid(items.size)
            } else {
                return@let CategoriesInvalid(errors = errors)
            }
        }

    private inline fun <reified T> readCsvFile(bytes: ByteArray): List<T> =
        StringReader(String(bytes)).use { reader ->
            return CsvMapper().configure(CsvParser.Feature.FAIL_ON_MISSING_COLUMNS, true).apply { registerModule(KotlinModule()) }
                .readerFor(T::class.java)
                .with(CsvSchema.emptySchema().withHeader().withLineSeparator(""))
                .readValues<T>(reader)
                .readAll()
                .toList()
        }
}

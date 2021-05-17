package com.boclips.videos.service.presentation.converters

import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvSchema
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.bson.types.ObjectId
import org.springframework.core.io.InputStreamSource
import java.io.StringReader

object CategoryMappingValidator {
    fun validate(input: InputStreamSource): CategoryValidationResult =
        readCsvFile<CategoryMappingMetadata>(input.inputStream.readBytes()).let { items ->
            var result: CategoryValidationResult = Valid(entries = 0)
            for ((index, item) in items.withIndex()) {
                if (item.categoryCode != "PST") {
                    result = InvalidCategoryCode(code = item.categoryCode!!)
                    break
                }
                result = try {
                    ObjectId(item.videoId)
                    Valid(entries = index + 1)
                } catch (e: IllegalArgumentException) {
                    InvalidVideoId(rowIndex = index, invalidId = item.videoId!!)
                }
            }
            return result
        }

    private inline fun <reified T> readCsvFile(bytes: ByteArray): List<T> =
        StringReader(String(bytes)).use { reader ->
            return CsvMapper().apply { registerModule(KotlinModule()) }
                .readerFor(T::class.java)
                .with(CsvSchema.emptySchema().withHeader().withLineSeparator(""))
                .readValues<T>(reader)
                .readAll()
                .toList()
        }
}

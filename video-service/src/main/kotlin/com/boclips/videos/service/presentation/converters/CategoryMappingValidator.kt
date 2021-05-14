package com.boclips.videos.service.presentation.converters

import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvParser
import org.bson.types.ObjectId
import java.lang.IllegalArgumentException

object CategoryMappingValidator {

    fun validate(file: ByteArray): CategoryValidationResult {
        val reader = CsvMapper().apply {
            this.enable(CsvParser.Feature.IGNORE_TRAILING_UNMAPPABLE)
            this.schemaFor(CategoryMappingMetadata::class.java)
                .withHeader()
                .withColumnReordering(true)
                .withNullValue("")
        }
        val items =
            reader.readerWithSchemaFor(CategoryMappingMetadata::class.java).readValues<CategoryMappingMetadata>(file)
                .readAll()
        var result: CategoryValidationResult = CategoryValidationResult.Valid(items.size)

        items.mapIndexed { index, categoryMappingMetadata ->
            {
                if (categoryMappingMetadata.videoId.isNullOrEmpty()) {
                    result = CategoryValidationResult.MissingVideoId(rowIndex = index)
                } else {
                    try {
                        ObjectId(categoryMappingMetadata.videoId)
                    } catch (ex: IllegalArgumentException) {
                        result = CategoryValidationResult.InvalidVideoId(
                            rowIndex = index,
                            invalidId = categoryMappingMetadata.videoId!!
                        )
                    }
                }

            }
        }
        return result
    }
}

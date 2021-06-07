package com.boclips.videos.service.presentation

import com.boclips.videos.service.presentation.converters.CategoryMappingMetadata

sealed class CategoryValidationResult

data class CategoriesValid(val entries: List<CategoryMappingMetadata>) : CategoryValidationResult()

sealed class CsvValidationError : CategoryValidationResult() {
    abstract fun getMessage(): String
}

object NotCsvFile : CsvValidationError() {
    override fun getMessage(): String = "The file is not a valid CSV format"
}

object VideoIdOrCategoryCodeColumnIsMissing : CsvValidationError() {
    override fun getMessage(): String = "The file must have both 'Category Code' and 'ID' columns"
}

data class DataRowsContainErrors(val errors: List<VideoTaggingValidationError>) : CsvValidationError() {
    override fun getMessage(): String {
        val errorMessages = emptyList<String>().toMutableList()
        errors.filterIsInstance<InvalidCategoryCode>().let { filteredErrors ->
            if (filteredErrors.isNotEmpty()) {
                errorMessages.add("Rows ${filteredErrors.joinToString { (it.rowIndex.toInt() + 1).toString() }} contain invalid or unknown category codes - ${
                    filteredErrors.map { it.code }.distinct().joinToString()
                }"
                )
            }
        }
        errors.filterIsInstance<MissingVideoId>().let { filteredErrors ->
            if (filteredErrors.isNotEmpty()) {
                errorMessages.add("Rows ${filteredErrors.joinToString { (it.rowIndex.toInt() + 1).toString() }} are missing a video ID")
            }
        }

        errors.filterIsInstance<InvalidVideoId>().let { filteredErrors ->
            if (filteredErrors.isNotEmpty()) {
                errorMessages.add("Rows ${
                    filteredErrors.joinToString { (it.rowIndex.toInt() + 1).toString() }
                } contain invalid Video IDs - ${filteredErrors.joinToString { it.invalidId }}"
                )
            }
        }

        return errorMessages.joinToString()
    }
}

sealed class VideoTaggingValidationError
data class InvalidCategoryCode(val rowIndex: Number, val code: String) : VideoTaggingValidationError()
data class InvalidVideoId(val rowIndex: Number, val invalidId: String) : VideoTaggingValidationError()
data class MissingVideoId(val rowIndex: Number) : VideoTaggingValidationError()

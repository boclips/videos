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
                errorMessages.add(
                    "Rows ${
                    filteredErrors.joinToString {
                        (it.getRowNumber().toString())
                    }
                    } contain invalid or unknown category codes - ${
                    filteredErrors.map { it.code }.distinct().joinToString()
                    }"
                )
            }
        }
        errors.filterIsInstance<MissingVideoId>().let { filteredErrors ->
            if (filteredErrors.isNotEmpty()) {
                errorMessages.add(
                    "Rows ${
                    filteredErrors.joinToString {
                        (it.getRowNumber().toString())
                    }
                    } are missing a video ID"
                )
            }
        }

        errors.filterIsInstance<InvalidVideoId>().let { filteredErrors ->
            if (filteredErrors.isNotEmpty()) {
                errorMessages.add(
                    "Rows ${
                    filteredErrors.joinToString { it.getRowNumber().toString() }
                    } contain invalid Video IDs - ${filteredErrors.joinToString { it.invalidId }}"
                )
            }
        }

        return errorMessages.joinToString()
    }
}

sealed class VideoTaggingValidationError {
    abstract val rowIndex: Int

    fun getRowNumber(): Int {
        // 0 index + header
        return rowIndex + 2
    }
}

data class InvalidCategoryCode(override val rowIndex: Int, val code: String) : VideoTaggingValidationError()
data class InvalidVideoId(override val rowIndex: Int, val invalidId: String) : VideoTaggingValidationError()
data class MissingVideoId(override val rowIndex: Int) : VideoTaggingValidationError()

package com.boclips.videos.service.presentation

import com.boclips.videos.service.presentation.converters.CategoryMappingMetadata

sealed class CategoryValidationResult

data class CsvValidated(val entries: List<CategoryMappingMetadata>) : CategoryValidationResult()

data class CsvValidatedWithEmptyIds(
    val entriesWithIds: List<CategoryMappingMetadata>,
    val entriesWithoutIds: List<CategoryMappingMetadata>
) : CategoryValidationResult()

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

        errors.filterIsInstance<VideoDoesntExist>().let { filteredErrors ->
            if (filteredErrors.isNotEmpty()) {
                errorMessages.add(
                    "Rows ${
                    filteredErrors.joinToString {
                        (it.getRowNumber().toString())
                    }
                    } this video ID doesn't exist"
                )
            }
        }

        errors.filterIsInstance<InvalidPedagogyTags>().let { filteredErrors ->
            if (filteredErrors.isNotEmpty()) {
                errorMessages.add(
                    "Rows ${
                    filteredErrors.joinToString {
                        (it.getRowNumber().toString())
                    }
                    } contain invalid Pedagogy Tags"
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
data class VideoDoesntExist(override val rowIndex: Int, val videoId: String) : VideoTaggingValidationError()
data class InvalidPedagogyTags(override val rowIndex: Int, val tag: String) : VideoTaggingValidationError()

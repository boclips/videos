package com.boclips.videos.service.presentation

import com.boclips.videos.service.presentation.converters.customMetadata.CustomMappingMetadata

sealed class CustomMetadataValidationResult()
data class CustomMetadataValidated(val entries: List<CustomMappingMetadata>) : CustomMetadataValidationResult()

sealed class CsvValidationMetadataError : CustomMetadataValidationResult() {
    abstract fun getMessage(): String
}

object NotCsvMetadataFile : CsvValidationMetadataError() {
    override fun getMessage(): String = "The file is not a valid CSV format"
}

data class MissingColumn(private val missingColumnName: String) : CsvValidationMetadataError() {
    override fun getMessage(): String {
        return "Invalid CSV - column name: $missingColumnName is missing"
    }
}

data class DataRowsContainMetadataErrors(val errors: List<RowValidationError>) : CsvValidationMetadataError() {
    override fun getMessage(): String {
        val errorMessages = emptyList<String>().toMutableList()

        errors.filterIsInstance<VideoIdMetadataDoesntExist>().let { filteredErrors ->
            if (filteredErrors.isNotEmpty()) {
                errorMessages.add(
                    "Rows ${
                    filteredErrors.joinToString {
                        (it.getRowNumber().toString())
                    }
                    } contains invalid video ID"
                )
            }
        }

        return errorMessages.joinToString()
    }
}

sealed class RowValidationError {
    abstract val rowIndex: Int

    fun getRowNumber(): Int {
        // 0 index + header
        return rowIndex + 2
    }
}

data class VideoIdMetadataDoesntExist(override val rowIndex: Int, val videoId: String) : RowValidationError()

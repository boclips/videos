package com.boclips.videos.service.presentation.converters


sealed class CategoryValidationResult

data class CategoriesValid(val entries: Number) : CategoryValidationResult()
data class CategoriesInvalid(val errors: List<CategoryValidationError>) : CategoryValidationResult() {
    fun getMessage(): String {
        val errorMessages = emptyList<String>().toMutableList()
        errorMessages.add(errors.filterIsInstance<InvalidCategoryCode>().let { filteredErrors ->
            "Rows ${
                filteredErrors.joinToString { it.rowIndex.toString() }
            } contain invalid or unknown category codes ${filteredErrors.joinToString { it.code }}"
        })
        errorMessages.add(errors.filterIsInstance<MissingVideoId>().let { filteredErrors ->
            "Rows ${filteredErrors.joinToString { it.rowIndex.toString() }} are missing a video ID"
        })
        errorMessages.add(errors.filterIsInstance<InvalidVideoId>().let { filteredErrors ->
            "Rows ${
                filteredErrors.joinToString { it.rowIndex.toString() }
            } contain invalid Video IDs - ${filteredErrors.joinToString { it.invalidId }}"
        })
        errorMessages.add(errors.filterIsInstance<InvalidFile>().let {
            "The file is not a valid CSV format"
        })
        return errorMessages.joinToString()
    }
}

sealed class CategoryValidationError
data class InvalidCategoryCode(val rowIndex: Number, val code: String) : CategoryValidationError()
data class InvalidVideoId(val rowIndex: Number, val invalidId: String) : CategoryValidationError()
data class MissingVideoId(val rowIndex: Number) : CategoryValidationError()
object InvalidFile : CategoryValidationError()

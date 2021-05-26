package com.boclips.videos.service.presentation.converters


sealed class CategoryValidationResult

data class CategoriesValid(val entries: Number) : CategoryValidationResult()

sealed class CsvValidationError: CategoryValidationResult() {
    abstract fun getMessage(): String
}
object NotCsvFile : CsvValidationError() {
    override fun getMessage(): String = "The file is not a valid CSV format"
}
object InvalidColumns : CsvValidationError() {
    override fun getMessage(): String = "The file must have both 'Category Code' and 'ID' columns"
}
data class CategoriesInvalid(val errors: List<CategoryValidationError>) : CsvValidationError() {
    override fun getMessage(): String {
        val errorMessages = emptyList<String>().toMutableList()
        errors.filterIsInstance<InvalidCategoryCode>().let { filteredErrors ->
            if (filteredErrors.isNotEmpty()) {
                errorMessages.add("Rows ${filteredErrors.joinToString { (it.rowIndex.toInt() + 1).toString() }} contain invalid or unknown category codes - ${filteredErrors.map { it.code }.distinct().joinToString()}"
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

        errors.filterIsInstance<InvalidFile>().let { filteredErrors ->
            if (filteredErrors.isNotEmpty()) {
                errorMessages.add("The file is not a valid CSV format")
            }
        }
        return errorMessages.joinToString()
    }
}

sealed class CategoryValidationError
data class InvalidCategoryCode(val rowIndex: Number, val code: String) : CategoryValidationError()
data class InvalidVideoId(val rowIndex: Number, val invalidId: String) : CategoryValidationError()
data class MissingVideoId(val rowIndex: Number) : CategoryValidationError()
object InvalidFile : CategoryValidationError()

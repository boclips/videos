package com.boclips.videos.service.presentation.converters


sealed class CategoryValidationResult

data class CategoriesValid(val entries: Number) : CategoryValidationResult()
data class CategoriesInvalid(val errors: List<CategoryValidationError>) : CategoryValidationResult()  {
    fun getMessage(): String {
        val errorMessage = ""
        errors.map { error ->
            when(error){
                is InvalidCategoryCode -> errorMessage.plus("Rows ${error.rowIndex} contain invalid or unknown category codes ${error.code} \n")
                is InvalidFile -> TODO()
                is InvalidVideoId -> TODO()
                is MissingVideoId -> TODO()
            }
        }
    }
}

sealed class CategoryValidationError
data class InvalidCategoryCode(val rowIndex: Number, val code: String) : CategoryValidationError()
data class InvalidVideoId(val rowIndex: Number, val invalidId: String) : CategoryValidationError()
data class MissingVideoId(val rowIndex: Number) : CategoryValidationError()
object InvalidFile : CategoryValidationError()

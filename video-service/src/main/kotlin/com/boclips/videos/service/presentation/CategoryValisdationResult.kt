package com.boclips.videos.service.presentation.converters

sealed class CategoryValidationResult {
abstract val message: String
abstract val isValid: Boolean

    data class Valid(val entries: Number) : CategoryValidationResult() {
        override val message = "Valid CSV, ${entries} parsed"
        override val isValid = true
    }

    data class InvalidCategoryCode(val code: String) : CategoryValidationResult() {
        override val message = "Invalid CSV, ${code} is not a valid Category code"
        override val isValid = false
    }

    data class InvalidVideoId(val rowIndex: Number, val invalidId: String) : CategoryValidationResult() {
        override val message = "Invalid CSV, ${invalidId} at row ${rowIndex} is not a valid VideoId"
        override val isValid = false
    }

    data class MissingVideoId(val rowIndex: Number) : CategoryValidationResult() {
        override val message = "Invalid CSV, missing videoID at row ${rowIndex}"
        override val isValid = false
    }
}

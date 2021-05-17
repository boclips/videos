package com.boclips.videos.service.presentation.converters

sealed class CategoryValidationResult

data class Valid(val entries: Number) : CategoryValidationResult()
data class InvalidCategoryCode(val code: String) : CategoryValidationResult()
data class InvalidVideoId(val rowIndex: Number, val invalidId: String) : CategoryValidationResult()
data class MissingVideoId(val rowIndex: Number) : CategoryValidationResult()

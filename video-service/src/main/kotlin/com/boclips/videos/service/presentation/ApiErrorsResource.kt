package com.boclips.videos.service.presentation

data class ApiErrorsResource(val errors: List<ApiErrorResource>)

data class ApiErrorResource(val field: String, val message: String)

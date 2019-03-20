package com.boclips.videos.service.presentation.event

import com.boclips.videos.service.application.analytics.InvalidEventException

data class CreateNoSearchResultsEventCommand(
    val name: String?,
    val email: String?,
    val query: String?,
    val description: String?
) : EventCommand() {
    override fun isValidOrThrows() {
        if (this.query.isNullOrBlank()) throw InvalidEventException("Email address must be specified")
        if (this.email.isNullOrBlank()) throw InvalidEventException("Query must be specified")
    }
}


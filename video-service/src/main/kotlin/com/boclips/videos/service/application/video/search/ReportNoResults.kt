package com.boclips.videos.service.application.video.search

import com.boclips.videos.service.application.event.InvalidEventException
import com.boclips.videos.service.infrastructure.email.EmailClient
import com.boclips.videos.service.infrastructure.email.NoResultsEmail
import com.boclips.videos.service.presentation.event.CreateNoSearchResultsEventCommand

class ReportNoResults(
        private val emailClient: EmailClient
) {

    fun execute(event: CreateNoSearchResultsEventCommand?) {
        event ?: throw InvalidEventException("Event cannot be null")
        event.isValidOrThrows()

        emailClient.send(
                NoResultsEmail(
                        name = event.name,
                        email = event.email,
                        query = event.query,
                        description = event.description
                )
        )
    }
}
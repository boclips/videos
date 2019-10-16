package com.boclips.videos.service.application.video.search

import com.boclips.videos.service.infrastructure.email.EmailClient
import com.boclips.videos.service.presentation.event.CreateNoSearchResultsEventCommand
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito

class ReportNoResultsTest {

    lateinit var emailClient: EmailClient

    lateinit var reportNoResults: ReportNoResults

    @BeforeEach
    fun setUp() {
        emailClient = Mockito.mock(EmailClient::class.java)
        reportNoResults = ReportNoResults(emailClient)
    }

    @Test
    fun `sends email to log no search results event`() {
        reportNoResults.execute(
            CreateNoSearchResultsEventCommand(
                name = "Hans",
                email = "hi@there.com",
                description = "none",
                query = "animal"
            )
        )

        verify(emailClient).send(any())
    }
}

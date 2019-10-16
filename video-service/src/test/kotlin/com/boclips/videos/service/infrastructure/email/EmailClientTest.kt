package com.boclips.videos.service.infrastructure.email

import com.nhaarman.mockitokotlin2.any
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mockito
import org.simplejavamail.email.Email
import org.simplejavamail.mailer.Mailer
import org.springframework.mock.env.MockEnvironment

class EmailClientTest {
    private lateinit var mailerMock: Mailer
    private lateinit var emailService: EmailClient

    @BeforeEach
    fun setUp() {
        mailerMock = Mockito.mock(Mailer::class.java)
        emailService = EmailClient(mailerMock, mockEnvironment())
    }

    @Test
    fun `search results event email contains subject with environment information`() {
        emailService.send(exampleEvent())

        val argument: ArgumentCaptor<Email> = ArgumentCaptor.forClass(Email::class.java)
        Mockito.verify(mailerMock).sendMail(argument.capture(), any())

        assertThat(argument.value.subject).isEqualTo("TEST: No search result event logged")
    }

    @Test
    fun `search results event email contains information about event`() {
        emailService.send(exampleEvent())

        val argument: ArgumentCaptor<Email> = ArgumentCaptor.forClass(Email::class.java)
        Mockito.verify(mailerMock).sendMail(argument.capture(), any())

        assertThat(argument.value.plainText).contains("Name: ")
        assertThat(argument.value.plainText).contains("Email: ")
        assertThat(argument.value.plainText).contains("Query: ")
        assertThat(argument.value.plainText).contains("Description: ")
    }

    private fun exampleEvent() =
        NoResultsEmail("Hans", "hans@coolcat.com", "stupid query", "This is some additional information")

    private fun mockEnvironment(): MockEnvironment {
        val environment = MockEnvironment()
        environment.addActiveProfile("test")
        return environment
    }
}

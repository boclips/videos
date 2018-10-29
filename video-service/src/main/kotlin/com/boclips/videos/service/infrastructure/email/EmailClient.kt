package com.boclips.videos.service.infrastructure.email

import mu.KLogging
import org.simplejavamail.email.Email
import org.simplejavamail.email.EmailBuilder
import org.simplejavamail.mailer.Mailer
import org.springframework.core.env.Environment


class EmailClient(
        private val mailer: Mailer,
        private val environment: Environment
) {
    companion object : KLogging()

    fun send(email: NoResultsEmail) {
        try {
            mailer.sendMail(createEmail(email), true)
            logger.info { "Successfully sent email" }
        } catch (ex: Exception) {
            logger.error { "Failed sending out email: $ex" }
        }
    }

    private fun createEmail(email: NoResultsEmail): Email? {
        val activeProfile = environment.activeProfiles.first()?.toUpperCase()

        return EmailBuilder
                .startingBlank()
                .to("logging@boclips.com")
                .from("noreply@boclips.com")
                .withSubject("$activeProfile: No search result event logged")
                .withPlainText(email.toPlainText())
                .withReplyTo("noreply", "noreply@boclips.com")
                .buildEmail()

    }
}
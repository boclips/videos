package com.boclips.videos.service.config

import com.boclips.videos.service.config.properties.EmailProperties
import com.boclips.videos.service.infrastructure.email.EmailClient
import org.simplejavamail.mailer.Mailer
import org.simplejavamail.mailer.MailerBuilder
import org.simplejavamail.mailer.config.TransportStrategy
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment

@Configuration
class EmailConfig {
    @Bean
    fun emailClient(mailer: Mailer, environment: Environment): EmailClient {
        return EmailClient(mailer, environment)
    }

    @Bean
    fun simpleJavaMailer(emailProperties: EmailProperties): Mailer {
        return MailerBuilder
                .withSMTPServer(emailProperties.host, emailProperties.port, emailProperties.username, emailProperties.password)
                .withTransportStrategy(TransportStrategy.SMTP_TLS)
                .clearEmailAddressCriteria()
                .buildMailer()
    }
}
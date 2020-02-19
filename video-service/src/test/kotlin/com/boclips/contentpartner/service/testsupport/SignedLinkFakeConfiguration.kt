package com.boclips.contentpartner.service.testsupport

import com.boclips.contentpartner.service.domain.model.SignedLinkProvider
import com.boclips.contentpartner.service.infrastructure.TestSignedLinkProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile

@Profile("fakes-signed-link")
@Configuration
class SignedLinkFakeConfiguration {

    @Bean
    @Primary
    fun fakeSignedLinkProvider(): SignedLinkProvider {
        return TestSignedLinkProvider()
    }
}

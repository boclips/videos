package com.boclips.contentpartner.service.config

import com.boclips.contentpartner.service.domain.service.EventConverter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration("contentPartnerDomainContext")
class DomainContext {

    @Bean
    fun eventConverter(): EventConverter {
        return EventConverter()
    }
}

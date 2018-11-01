package com.boclips.videos.service.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate

@Configuration
class DataConfig {
    @Bean
    @Profile("!test")
    fun namedJdbcTemplate(jdbcTemplate: JdbcTemplate): NamedParameterJdbcTemplate {
        jdbcTemplate.fetchSize = Int.MIN_VALUE
        return NamedParameterJdbcTemplate(jdbcTemplate)
    }
}
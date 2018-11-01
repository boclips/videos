package com.boclips.videos.service.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate


@Configuration
class TestConfig {

    @Bean
    @Primary
    fun testNamedJdbcTemplate(jdbcTemplate: JdbcTemplate): NamedParameterJdbcTemplate {
        jdbcTemplate.fetchSize = 100
        return NamedParameterJdbcTemplate(jdbcTemplate)
    }
}

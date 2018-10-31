package com.boclips.search.service.infrastructure

data class ElasticSearchConfig(
        val scheme: String,
        val host: String,
        val port: Int,
        val username: String,
        val password: String
)
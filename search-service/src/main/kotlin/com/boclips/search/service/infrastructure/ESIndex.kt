package com.boclips.search.service.infrastructure

import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

interface ESIndex {

    fun getIndexAlias() : String
    fun getIndexWildcard() : String

    companion object {
        fun generateIndexName(prefix: String): String {
            val timestamp = ZonedDateTime.now(ZoneId.of("UTC"))
                .format(DateTimeFormatter.ofPattern("yyyyMMddHmsn"))
            return "$prefix$timestamp"
        }
    }
}
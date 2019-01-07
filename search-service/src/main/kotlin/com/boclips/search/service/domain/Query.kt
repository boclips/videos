package com.boclips.search.service.domain

import kotlin.reflect.KProperty1

class Filter private constructor(val field: String, val value: Any) {

    companion object {
        operator fun <T : Any> invoke(field: KProperty1<VideoMetadata, T>, value: T) = Filter(field.name, value)
    }
}

data class Query(val phrase: String? = null, val ids: List<String>? = null, val filters: List<Filter> = emptyList()) {

    companion object {

        private val ID_QUERY_REGEX = "id:(\\S+)".toRegex()

        fun parse(query: String): Query {
            val match = ID_QUERY_REGEX.matchEntire(query)
            if(match != null) {
                val ids = match.groupValues[1].split(',')
                return Query(ids = ids)
            }
            return Query(phrase = query)
        }

    }

    fun withFilters(filters: List<Filter>) = copy(filters = filters)
}

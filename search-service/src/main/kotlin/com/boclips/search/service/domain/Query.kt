package com.boclips.search.service.domain

data class Query(val phrase: String? = null, val ids: List<String>? = null) {

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
}

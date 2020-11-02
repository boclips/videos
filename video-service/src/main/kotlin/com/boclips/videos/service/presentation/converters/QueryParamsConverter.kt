package com.boclips.videos.service.presentation.converters

class QueryParamsConverter {
    companion object {
        private val PARAMS_NOT_FOR_SPLITTING = listOf("query")
        fun toSplitList(queryParams: Map<String, Array<String>>): Map<String, List<String>> {
            return queryParams.mapValues { entry ->
                if (!PARAMS_NOT_FOR_SPLITTING.contains(entry.key)) {
                    entry.value.map { it.split(",") }.flatten()
                } else entry.value.toList()
            }
        }
    }
}

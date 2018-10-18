package com.boclips.search.service.infrastructure

class IndexConfiguration {

    companion object {
        const val FIELD_DESCRIPTOR_SHINGLES = "shingles"
    }

    fun generateIndexSettings(): Map<String, Any> {
        return mapOf("analysis" to mapOf(
                "filter" to mapOf(
                        "shingle_filter" to mapOf(
                                "type" to "shingle",
                                "min_shingle_size" to 2,
                                "max_shingle_size" to 2,
                                "output_unigrams" to false
                        )
                ),
                "analyzer" to mapOf(
                        "shingle_analyzer" to mapOf(
                                "type" to "custom",
                                "tokenizer" to "standard",
                                "filter" to listOf(
                                        "lowercase",
                                        "shingle_filter"
                                )
                        )
                )
        ))
    }

    fun generateVideoMapping(): Map<String, Any> {
        val freeTextField = mapOf(
                "type" to "text",
                "analyzer" to "english",
                "fields" to mapOf(
                        FIELD_DESCRIPTOR_SHINGLES to mapOf("type" to "text", "analyzer" to "shingle_analyzer")
                )
        )
        val keywordField = mapOf(
                "type" to "text",
                "analyzer" to "english",
                "position_increment_gap" to 100
        )
        return mapOf("properties" to mapOf(
                "title" to freeTextField,
                "description" to freeTextField,
                "keywords" to keywordField
        ))
    }

}

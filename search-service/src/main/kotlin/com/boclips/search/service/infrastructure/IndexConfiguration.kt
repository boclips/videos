package com.boclips.search.service.infrastructure

class IndexConfiguration {

    companion object {
        const val FIELD_DESCRIPTOR_SHINGLES = "shingles"

        val synonyms = IndexConfiguration::class.java.getResource("/synonyms/english.txt").readText().trim().split("\n")
    }

    fun generateIndexSettings(): Map<String, Any> {
        return mapOf(
                "analysis" to mapOf(
                        "filter" to mapOf(
                                "shingle_filter" to mapOf(
                                        "type" to "shingle",
                                        "min_shingle_size" to 2,
                                        "max_shingle_size" to 2,
                                        "output_unigrams" to false
                                ),
                                "synonym_filter" to mapOf(
                                        "type" to "synonym",
                                        "synonyms" to synonyms,
                                        "expand" to false
                                ),
                                "english_stop" to mapOf(
                                        "type" to "stop",
                                        "stopwords" to "_english_"
                                ),
                                "english_stemmer" to mapOf(
                                        "type" to "stemmer",
                                        "language" to "english"
                                ),
                                "english_possessive_stemmer" to mapOf(
                                        "type" to "stemmer",
                                        "language" to "possessive_english"
                                )
                        ),
                        "analyzer" to mapOf(
                                "english_analyser" to mapOf(
                                        "tokenizer" to "standard",
                                        "filter" to listOf(
                                                "english_possessive_stemmer",
                                                "lowercase",
                                                "english_stemmer",
                                                "synonym_filter",
                                                "english_stop"
                                        )
                                ),
                                "shingle_analyzer" to mapOf(
                                        "type" to "custom",
                                        "tokenizer" to "standard",
                                        "filter" to listOf(
                                                "lowercase",
                                                "shingle_filter"
                                        )
                                )
                        )
                )
        )
    }

    fun generateVideoMapping(): Map<String, Any> {
        val freeTextField = mapOf(
                "type" to "text",
                "analyzer" to "english_analyser",
                "fields" to mapOf(
                        FIELD_DESCRIPTOR_SHINGLES to mapOf("type" to "text", "analyzer" to "shingle_analyzer")
                )
        )
        val keywordField = mapOf(
                "type" to "text",
                "analyzer" to "english_analyser",
                "position_increment_gap" to 100
        )
        return mapOf(
                "properties" to mapOf(
                        "title" to freeTextField,
                        "description" to freeTextField,
                        "contentProvider" to freeTextField,
                        "keywords" to keywordField
                )
        )
    }
}

package com.boclips.search.service.infrastructure

class IndexConfiguration {

    companion object {
        const val FIELD_DESCRIPTOR_SHINGLES = "shingles"

        val synonyms = loadSynonyms("english.txt")
        val synonymsCaseSensitive = loadSynonyms("english-case-sensitive.txt")

        private fun loadSynonyms(filename: String) = IndexConfiguration::class.java.getResource("/synonyms/$filename")
                .readText().trim().split("\n")
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
                                "english_synonym_filter" to mapOf(
                                        "type" to "synonym",
                                        "synonyms" to synonyms,
                                        "expand" to false
                                ),
                                "english_synonym_case_sensitive_filter" to mapOf(
                                        "type" to "synonym",
                                        "synonyms" to synonymsCaseSensitive,
                                        "expand" to false
                                ),
                                "english_stop_filter" to mapOf(
                                        "type" to "stop",
                                        "stopwords" to "_english_"
                                ),
                                "english_stemmer_filter" to mapOf(
                                        "type" to "stemmer",
                                        "language" to "english"
                                ),
                                "english_possessive_stemmer_filter" to mapOf(
                                        "type" to "stemmer",
                                        "language" to "possessive_english"
                                )
                        ),
                        "analyzer" to mapOf(
                                "english_analyzer" to mapOf(
                                        "tokenizer" to "standard",
                                        "filter" to listOf(
                                                "english_possessive_stemmer_filter",
                                                "english_synonym_case_sensitive_filter",
                                                "lowercase",
                                                "english_stemmer_filter",
                                                "english_synonym_filter",
                                                "english_stop_filter"
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
                        ),
                        "normalizer" to mapOf(
                            "lowercase_normalizer" to mapOf(
                                    "type" to "custom",
                                    "filter" to listOf("lowercase")
                            )
                        )
                )
        )
    }

    fun generateVideoMapping(): Map<String, Any> {
        val freeTextField = mapOf(
                "type" to "text",
                "analyzer" to "english_analyzer",
                "fields" to mapOf(
                        FIELD_DESCRIPTOR_SHINGLES to mapOf("type" to "text", "analyzer" to "shingle_analyzer")
                )
        )
        val contentPartnerField = mapOf(
                "type" to "keyword",
                "normalizer" to "lowercase_normalizer"
        )
        val keywordField = mapOf(
                "type" to "text",
                "analyzer" to "english_analyzer",
                "position_increment_gap" to 100
        )
        return mapOf(
                "properties" to mapOf(
                        "title" to freeTextField,
                        "description" to freeTextField,
                        "contentProvider" to contentPartnerField,
                        "keywords" to keywordField
                )
        )
    }
}

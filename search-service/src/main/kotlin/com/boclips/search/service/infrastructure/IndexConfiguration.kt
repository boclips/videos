package com.boclips.search.service.infrastructure

class IndexConfiguration {

    companion object {
        const val FIELD_DESCRIPTOR_SHINGLES = "shingles"

        object Analyzers {
            const val ENGLISH = "english_analyzer"
            const val ENGLISH_SEARCH = "english_search_analyzer"
            const val SHINGLES = "shingle_analyzer"
        }

        object Filters {
            const val PREDEFINED_LOWERCASE = "lowercase"

            const val SHINGLES = "shingle_filter"
            const val ENGLISH_SYNONYMS = "english_synonym_filter"
            const val ENGLISH_SYNONYMS_CASE_SENSITIVE = "english_synonym_case_sensitive_filter"
            const val ENGLISH_STOP = "english_stop_filter"
            const val ENGLISH_STEMMER = "english_stemmer_filter"
            const val ENGLISH_POSSESSIVE_STEMMER = "english_possessive_stemmer_filter"
        }

        object Normalizers {
            const val LOWERCASE = "lowercase_normalizer"
        }

        val synonyms = loadSynonyms("english.txt")
        val synonymsCaseSensitive = loadSynonyms("english-case-sensitive.txt")

        private fun loadSynonyms(filename: String) = IndexConfiguration::class.java.getResource("/synonyms/$filename")
            .readText().trim().split("\n")
    }

    fun generateIndexSettings(): Map<String, Any> {
        return mapOf(
            "analysis" to mapOf(
                "filter" to mapOf(
                    Filters.SHINGLES to mapOf(
                        "type" to "shingle",
                        "min_shingle_size" to 2,
                        "max_shingle_size" to 2,
                        "output_unigrams" to false
                    ),
                    Filters.ENGLISH_SYNONYMS to mapOf(
                        "type" to "synonym_graph",
                        "synonyms" to synonyms,
                        "expand" to true
                    ),
                    Filters.ENGLISH_SYNONYMS_CASE_SENSITIVE to mapOf(
                        "type" to "synonym",
                        "synonyms" to synonymsCaseSensitive,
                        "expand" to false
                    ),
                    Filters.ENGLISH_STOP to mapOf(
                        "type" to "stop",
                        "stopwords" to "_english_"
                    ),
                    Filters.ENGLISH_STEMMER to mapOf(
                        "type" to "stemmer",
                        "language" to "english"
                    ),
                    Filters.ENGLISH_POSSESSIVE_STEMMER to mapOf(
                        "type" to "stemmer",
                        "language" to "possessive_english"
                    )
                ),
                "analyzer" to mapOf(
                    Analyzers.ENGLISH to mapOf(
                        "tokenizer" to "standard",
                        "filter" to listOf(
                            Filters.ENGLISH_POSSESSIVE_STEMMER,
                            Filters.ENGLISH_SYNONYMS_CASE_SENSITIVE,
                            Filters.PREDEFINED_LOWERCASE,
                            Filters.ENGLISH_STEMMER,
                            Filters.ENGLISH_STOP
                        )
                    ),
                    Analyzers.ENGLISH_SEARCH to mapOf(
                        "tokenizer" to "standard",
                        "filter" to listOf(
                            Filters.ENGLISH_POSSESSIVE_STEMMER,
                            Filters.ENGLISH_SYNONYMS_CASE_SENSITIVE,
                            Filters.PREDEFINED_LOWERCASE,
                            Filters.ENGLISH_STEMMER,
                            Filters.ENGLISH_SYNONYMS,
                            Filters.ENGLISH_STOP
                        )
                    ),
                    Analyzers.SHINGLES to mapOf(
                        "type" to "custom",
                        "tokenizer" to "standard",
                        "filter" to listOf(
                            Filters.PREDEFINED_LOWERCASE,
                            Filters.SHINGLES
                        )
                    )
                ),
                "normalizer" to mapOf(
                    Normalizers.LOWERCASE to mapOf(
                        "type" to "custom",
                        "filter" to listOf(Filters.PREDEFINED_LOWERCASE)
                    )
                )
            )
        )
    }

    fun generateVideoMapping(): Map<String, Any> {
        val freeTextField = mapOf(
            "type" to "text",
            "analyzer" to Analyzers.ENGLISH,
            "search_analyzer" to Analyzers.ENGLISH_SEARCH,
            "fields" to mapOf(
                FIELD_DESCRIPTOR_SHINGLES to mapOf("type" to "text", "analyzer" to Analyzers.SHINGLES)
            )
        )
        val contentPartnerField = mapOf(
            "type" to "keyword",
            "normalizer" to Normalizers.LOWERCASE
        )
        val keywordField = mapOf(
            "type" to "text",
            "analyzer" to Analyzers.ENGLISH,
            "position_increment_gap" to 100
        )
        val dateField = mapOf(
            "type" to "date"
        )
        return mapOf(
            "properties" to mapOf(
                "title" to freeTextField,
                "description" to freeTextField,
                "contentProvider" to contentPartnerField,
                "releaseDate" to dateField,
                "transcript" to freeTextField,
                "keywords" to keywordField
            )
        )
    }
}

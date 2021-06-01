package com.boclips.search.service.infrastructure

interface IndexConfiguration {
    fun generateMapping(): Map<String, Any>

    companion object {
        const val FIELD_DESCRIPTOR_UNSTEMMED = "unstemmed"
        const val FIELD_DESCRIPTOR_KEYWORD = "keyword"

        object Analyzers {
            const val ENGLISH = "english_analyzer"
            const val ENGLISH_SEARCH = "english_search_analyzer"
            const val UNSTEMMED = "unstemmed_analyzer"
            const val UNSTEMMED_SYNONYMS = "unstemmed_synonyms"
            const val NGRAM = "ngram"
        }

        object Filters {
            const val PREDEFINED_LOWERCASE = "lowercase"

            const val SHINGLES = "shingle_filter"
            const val ENGLISH_SYNONYMS = "english_synonym_filter"
            const val ENGLISH_SYNONYMS_CASE_SENSITIVE = "english_synonym_case_sensitive_filter"
            const val ENGLISH_STOP = "english_stop_filter"
            const val ENGLISH_STEMMER = "english_stemmer_filter"
            const val ENGLISH_POSSESSIVE_STEMMER = "english_possessive_stemmer_filter"

            const val NGRAM = "ngram_filter"
        }

        object Normalizers {
            const val LOWERCASE = "lowercase_normalizer"
        }

        val synonyms = loadSynonyms("english.txt")
        val synonymsCaseSensitive = loadSynonyms("english-case-sensitive.txt")

        private fun loadSynonyms(filename: String) = IndexConfiguration::class.java.getResource("/synonyms/$filename")
            .readText().trim().split("\n")

        fun unstemmed(fieldName: String) = "$fieldName.$FIELD_DESCRIPTOR_UNSTEMMED"
    }

    object Fields {
        val simpleText = mapOf(
            "type" to "keyword"
        )

        val caseInsensitiveSimpleText = mapOf(
            "type" to "keyword",
            "normalizer" to Normalizers.LOWERCASE
        )

        val autocomplete = mapOf(
            "search_analyzer" to "standard",
            "analyzer" to Analyzers.NGRAM,
            "fields" to mapOf(
                FIELD_DESCRIPTOR_UNSTEMMED to mapOf(
                    "type" to "text",
                    "analyzer" to Analyzers.UNSTEMMED
                ),
            ),
            "type" to "text"
        )

        val freeTextSortable = mapOf(
            "type" to "text",
            "analyzer" to Analyzers.ENGLISH,
            "search_analyzer" to Analyzers.ENGLISH_SEARCH,
            "fields" to mapOf(
                FIELD_DESCRIPTOR_UNSTEMMED to mapOf(
                    "type" to "text",
                    "analyzer" to Analyzers.UNSTEMMED
                ),
                FIELD_DESCRIPTOR_KEYWORD to mapOf(
                    "type" to "keyword"
                )
            )
        )

        val freeText = mapOf(
            "type" to "text",
            "analyzer" to Analyzers.ENGLISH,
            "search_analyzer" to Analyzers.ENGLISH_SEARCH,
            "fields" to mapOf(
                FIELD_DESCRIPTOR_UNSTEMMED to mapOf(
                    "type" to "text",
                    "analyzer" to Analyzers.UNSTEMMED
                )
            )
        )

        val simpleTextArray = mapOf(
            "type" to "keyword"
        )

        val stringArray = mapOf(
            "type" to "text",
            "analyzer" to Analyzers.UNSTEMMED_SYNONYMS
        )

        val date = mapOf(
            "type" to "date"
        )

        val boolean = mapOf(
            "type" to "boolean"
        )

        val double = mapOf(
            "type" to "double"
        )
    }

    fun defaultEnglishSettings(numberOfShards: Int): Map<String, Any> {
        return mapOf(
            "index" to mapOf(
                "number_of_shards" to numberOfShards,
                "number_of_replicas" to 1
            ),
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
                    ),
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
                    Analyzers.UNSTEMMED_SYNONYMS to mapOf(
                        "tokenizer" to "standard",
                        "filter" to listOf(
                            Filters.PREDEFINED_LOWERCASE,
                            Filters.ENGLISH_SYNONYMS
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
                    Analyzers.UNSTEMMED to mapOf(
                        "tokenizer" to "standard",
                        "filter" to listOf(
                            "lowercase"
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

    fun ngramSetting(numberOfShards: Int): Map<String, Any> {
        return mapOf(
            "index" to mapOf(
                "number_of_shards" to numberOfShards,
                "number_of_replicas" to 1,
                "max_ngram_diff" to 30
            ),
            "analysis" to mapOf(
                "filter" to mapOf(
                    Filters.NGRAM to mapOf(
                        "max_gram" to "30",
                        "min_gram" to "1",
                        "type" to "ngram",
                        "token_chars" to listOf(
                            "letter",
                            "digit",
                            "whitespace"
                        ),
                    ),
                    Filters.PREDEFINED_LOWERCASE to mapOf(
                        "type" to "lowercase"
                    )
                ),
                "analyzer" to mapOf(
                    Analyzers.NGRAM to mapOf(
                        "tokenizer" to "standard",
                        "filter" to listOf(
                            Filters.PREDEFINED_LOWERCASE,
                            Filters.NGRAM
                        )
                    ),
                    Analyzers.UNSTEMMED to mapOf(
                        "tokenizer" to "standard",
                        "filter" to listOf(
                            "lowercase"
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
}


package com.boclips.videos.service.presentation.converters

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class QueryParamsConverterTest {

    @Test
    fun `it splits all params by coma`() {
        val queryParams = mapOf(
            "age_range" to arrayOf("4-6,8-11"),
            "channel" to arrayOf("channel-id")
        )

        val convertedQueryParams = QueryParamsConverter.toSplitList(queryParams)
        assertThat(convertedQueryParams).hasSize(2)
        assertThat(convertedQueryParams["age_range"]).containsExactly("4-6", "8-11")
        assertThat(convertedQueryParams["channel"]).containsExactly("channel-id")
    }

    @Test
    fun `it doesn't split 'query' param`() {
        val queryParams = mapOf(
            "query" to arrayOf("the good, the bad and the ugly"),
            "channel" to arrayOf("channel-id")
        )

        val convertedQueryParams = QueryParamsConverter.toSplitList(queryParams)
        assertThat(convertedQueryParams).hasSize(2)
        assertThat(convertedQueryParams["query"]).containsExactly("the good, the bad and the ugly")
        assertThat(convertedQueryParams["channel"]).containsExactly("channel-id")
    }
}

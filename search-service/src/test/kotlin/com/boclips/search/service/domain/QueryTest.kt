package com.boclips.search.service.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class QueryTest {

    @Test
    fun `phrase query`() {
        assertThat(Query.parse("white girl dancing")).isEqualTo(Query(phrase = "white girl dancing"))
    }

    @Test
    internal fun `id query`() {
        assertThat(Query.parse("id:1,2")).isEqualTo(Query(ids = listOf("1", "2")))
    }
}

package com.boclips.videoanalyser.application.csv

import com.boclips.videoanalyser.domain.model.search.SearchExpectation
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test

class SearchExpectationCsvTest {

    @Test
    fun `toSearchExpectation trims whitespaces fields`() {
        val searchExpectation = SearchExpectationCsv("\tlinear equations\n", " 2352831\n\n")
                .toSearchExpectation()

        assertThat(searchExpectation).isEqualTo(SearchExpectation("linear equations", "2352831"))
    }

    @Test
    fun `toSearchExpectation throws if query is blank`() {
        val searchExpectation = SearchExpectationCsv(" ", "2352831")
        assertThatThrownBy { searchExpectation.toSearchExpectation() }.hasMessage("Empty query for asset 2352831")
    }

    @Test
    fun `toSearchExpectation throws if video id contains not-numeric characters`() {
        val searchExpectation = SearchExpectationCsv("linear equations", "235 2831")
        assertThatThrownBy { searchExpectation.toSearchExpectation() }.hasMessage("Invalid asset id: '235 2831'")
    }

    @Test
    fun `toSearchExpectation when video is blank`() {
        val searchExpectation = SearchExpectationCsv("linear equations", " ")
                .toSearchExpectation()

        assertThat(searchExpectation).isNull()
    }

}

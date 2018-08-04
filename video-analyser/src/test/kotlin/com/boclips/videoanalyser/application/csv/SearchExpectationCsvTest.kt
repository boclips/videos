package com.boclips.videoanalyser.application.csv

import com.boclips.videoanalyser.application.csv.SearchExpectationCsv
import com.boclips.videoanalyser.domain.model.search.SearchExpectation
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test

class SearchExpectationCsvTest {

    @Test
    fun toSearchExpectation_whenCorrectUrlFormat() {
        val searchExpectation = SearchExpectationCsv("linear equations", "http://boclips.com/video/2352831")
                .toSearchExpectation()

        assertThat(searchExpectation).isEqualTo(SearchExpectation("linear equations", "2352831"))
    }

    @Test
    fun toSearchExpectation_whenIncorrectUrlFormat() {

        assertThatThrownBy {
            SearchExpectationCsv("linear equations", "http://boclips.com/videodescriptors/5b44eb493d14223b2aea4bac")
                    .toSearchExpectation()
        }
                .hasMessage("Unexpected URL format: http://boclips.com/videodescriptors/5b44eb493d14223b2aea4bac")

    }

}

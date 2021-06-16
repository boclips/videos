package com.boclips.videos.service.application.video.search

import com.boclips.videos.service.domain.model.video.request.FixedAgeRangeFacet
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Duration

class FacetConverterTest {
    @Test
    fun `converts duration facets`() {
        val facets = FacetConverter().invoke(
            ageRangesFacets = null, durationFacets = listOf("PT0S-PT1M"),
            resourcesFacets = null, includeChannelFacets = null, videoTypesFacets = null, includePriceFacets = null
        )

        assertThat(facets.durations.first()).isEqualTo(Pair(Duration.ZERO, Duration.ofMinutes(1)))
    }

    @Test
    fun `converts age range facets`() {
        val ageRange = FixedAgeRangeFacet(min = 3, max = 5)
        val facets = FacetConverter().invoke(
            ageRangesFacets = listOf(ageRange), durationFacets = null,
            resourcesFacets = null, includeChannelFacets = null, videoTypesFacets = null, includePriceFacets = null
        )

        assertThat(facets.ageRanges.first()).isEqualTo(ageRange)
    }

    @Test
    fun `converts attachment type facets`() {
        val facets = FacetConverter().invoke(
            ageRangesFacets = null,
            durationFacets = null,
            resourcesFacets = listOf("Activity"),
            includeChannelFacets = null,
            videoTypesFacets = null,
            includePriceFacets = null
        )

        assertThat(facets.attachmentTypes.first()).isEqualTo("Activity")
    }

    @Test
    fun `converts video type facets`() {
        val facets = FacetConverter().invoke(
            ageRangesFacets = null, durationFacets = null, resourcesFacets = null,
            videoTypesFacets = listOf("Stock"), includeChannelFacets = null, includePriceFacets = null
        )

        assertThat(facets.videoTypes.first()).isEqualTo("Stock")
    }
}

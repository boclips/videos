package com.boclips.videos.service.presentation

import com.boclips.search.service.domain.common.Count
import com.boclips.search.service.domain.common.FacetCount
import com.boclips.search.service.domain.common.FacetType
import com.boclips.search.service.domain.videos.model.AgeRange
import com.boclips.search.service.domain.videos.model.DurationRange
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.asTeacher
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.Duration

class VideoControllerFacetsIntegrationTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `contains counts for subjects`() {
        videoSearchService.setFacets(
            listOf(
                FacetCount(
                    type = FacetType.Subjects,
                    counts = listOf(Count(id = "subject1", hits = 56))
                )
            )
        )

        mockMvc.perform(get("/v1/videos?query=content").asTeacher())
            .andExpect(status().isOk)
            .andExpect(header().string("Content-Type", "application/hal+json;charset=UTF-8"))
            .andExpect(jsonPath("$._embedded.facets.subjects.*", hasSize<Int>(1)))
            .andExpect(jsonPath("$._embedded.facets.subjects.subject1.hits", equalTo(56)))
    }

    @Test
    fun `contains count for age ranges`() {
        videoSearchService.setFacets(
            listOf(
                FacetCount(
                    type = FacetType.AgeRanges,
                    counts = listOf(Count(id = "agerange1", hits = 86))
                )
            )
        )

        mockMvc.perform(get("/v1/videos?query=content").asTeacher())
            .andExpect(status().isOk)
            .andExpect(header().string("Content-Type", "application/hal+json;charset=UTF-8"))
            .andExpect(jsonPath("$._embedded.facets.ageRanges.*", hasSize<Int>(1)))
            .andExpect(jsonPath("$._embedded.facets.ageRanges.agerange1.hits", equalTo(86)))
    }

    @Test
    fun `does not render if they don't exist`() {
        videoSearchService.setFacets(emptyList())

        mockMvc.perform(get("/v1/videos?query=content").asTeacher())
            .andExpect(status().isOk)
            .andExpect(header().string("Content-Type", "application/hal+json;charset=UTF-8"))
            .andExpect(jsonPath("$._embedded.facets.subjects[*]", hasSize<Int>(0)))
            .andExpect(jsonPath("$._embedded.facets.ageRanges[*]", hasSize<Int>(0)))
    }

    @Test
    fun `age_range_facets overwrite default age range facets`() {
        mockMvc.perform(get("/v1/videos?query=content&age_range_facets=3-7,11-13").asTeacher())
            .andExpect(status().isOk)

        val lastSearchRequest = videoSearchService.getLastSearchRequest()

        assertThat(lastSearchRequest.query.facetDefinition?.ageRangeBuckets).containsExactly(
            AgeRange(3, 7),
            AgeRange(11, 13)
        )
    }

    @Test
    fun `contains counts for durations`() {
        videoSearchService.setFacets(
            listOf(
                FacetCount(
                    type = FacetType.Duration,
                    counts = listOf(Count(id = "duration1", hits = 94))
                )
            )
        )

        mockMvc.perform(get("/v1/videos?query=content").asTeacher())
            .andExpect(status().isOk)
            .andExpect(header().string("Content-Type", "application/hal+json;charset=UTF-8"))
            .andExpect(jsonPath("$._embedded.facets.durations.*", hasSize<Int>(1)))
            .andExpect(jsonPath("$._embedded.facets.durations.duration1.hits", equalTo(94)))
    }

    @Test
    fun `duration_facets overwrite default duration facets`() {
        mockMvc.perform(get("/v1/videos?query=content&duration_facets=PT0S-PT5M,PT5M-PT10M").asTeacher())
            .andExpect(status().isOk)

        val lastSearchRequest = videoSearchService.getLastSearchRequest()

        assertThat(lastSearchRequest.query.facetDefinition?.duration).containsExactly(
            DurationRange(Duration.ZERO, Duration.ofMinutes(5)),
            DurationRange(Duration.ofMinutes(5), Duration.ofMinutes(10))
        )
    }
}


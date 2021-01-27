package com.boclips.videos.service.presentation

import com.boclips.search.service.domain.common.Count
import com.boclips.search.service.domain.common.FacetCount
import com.boclips.search.service.domain.common.FacetType
import com.boclips.search.service.domain.videos.model.AgeRange
import com.boclips.search.service.domain.videos.model.DurationRange
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.MvcMatchers.halJson
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
        val subject = saveSubject("Geography")
        val subjectId = subject.id.value

        videoIndexFake.setFacets(
            listOf(
                FacetCount(
                    type = FacetType.Subjects,
                    counts = listOf(Count(id = subjectId, hits = 56))
                )
            )
        )

        mockMvc.perform(get("/v1/videos?query=content").asTeacher(email = userAssignedToOrganisation().idOrThrow().value))
            .andExpect(status().isOk)
            .andExpect(halJson())
            .andExpect(jsonPath("$._embedded.facets.subjects.*", hasSize<Int>(1)))
            .andExpect(jsonPath("$._embedded.facets.subjects.$subjectId.hits", equalTo(56)))
            .andExpect(jsonPath("$._embedded.facets.subjects.$subjectId.id", equalTo(subjectId)))
            .andExpect(jsonPath("$._embedded.facets.subjects.$subjectId.name", equalTo("Geography")))
    }

    @Test
    fun `contains count for age ranges`() {
        videoIndexFake.setFacets(
            listOf(
                FacetCount(
                    type = FacetType.AgeRanges,
                    counts = listOf(Count(id = "agerange1", hits = 86))
                )
            )
        )

        mockMvc.perform(get("/v1/videos?query=content").asTeacher(email = userAssignedToOrganisation().idOrThrow().value))
            .andExpect(status().isOk)
            .andExpect(header().string("Content-Type", "application/hal+json"))
            .andExpect(jsonPath("$._embedded.facets.ageRanges.*", hasSize<Int>(1)))
            .andExpect(jsonPath("$._embedded.facets.ageRanges.agerange1.hits", equalTo(86)))
    }

    @Test
    fun `does not render if they don't exist`() {
        videoIndexFake.setFacets(emptyList())

        mockMvc.perform(get("/v1/videos?query=content").asTeacher(email = userAssignedToOrganisation().idOrThrow().value))
            .andExpect(status().isOk)
            .andExpect(header().string("Content-Type", "application/hal+json"))
            .andExpect(jsonPath("$._embedded.facets.subjects[*]", hasSize<Int>(0)))
            .andExpect(jsonPath("$._embedded.facets.ageRanges[*]", hasSize<Int>(0)))
    }

    @Test
    fun `age_range_facets overwrite default age range facets`() {
        mockMvc.perform(get("/v1/videos?query=content&age_range_facets=3-7,11-13").asTeacher(email = userAssignedToOrganisation().idOrThrow().value))
            .andExpect(status().isOk)

        val lastSearchRequest = videoIndexFake.getLastSearchRequest()

        assertThat(lastSearchRequest.query.facetDefinition?.ageRangeBuckets).containsExactly(
            AgeRange(3, 7),
            AgeRange(11, 13)
        )
    }

    @Test
    fun `contains counts for durations`() {
        videoIndexFake.setFacets(
            listOf(
                FacetCount(
                    type = FacetType.Duration,
                    counts = listOf(Count(id = "duration1", hits = 94))
                )
            )
        )

        mockMvc.perform(get("/v1/videos?query=content").asTeacher(email = userAssignedToOrganisation().idOrThrow().value))
            .andExpect(status().isOk)
            .andExpect(header().string("Content-Type", "application/hal+json"))
            .andExpect(jsonPath("$._embedded.facets.durations.*", hasSize<Int>(1)))
            .andExpect(jsonPath("$._embedded.facets.durations.duration1.hits", equalTo(94)))
    }

    @Test
    fun `duration_facets overwrite default duration facets`() {
        mockMvc.perform(get("/v1/videos?query=content&duration_facets=PT0S-PT5M,PT5M-PT10M").asTeacher(email = userAssignedToOrganisation().idOrThrow().value))
            .andExpect(status().isOk)

        val lastSearchRequest = videoIndexFake.getLastSearchRequest()

        assertThat(lastSearchRequest.query.facetDefinition?.duration).containsExactly(
            DurationRange(Duration.ZERO, Duration.ofMinutes(5)),
            DurationRange(Duration.ofMinutes(5), Duration.ofMinutes(10))
        )
    }

    @Test
    fun `contains counts for resource types`() {
        videoIndexFake.setFacets(
            listOf(
                FacetCount(
                    type = FacetType.AttachmentTypes,
                    counts = listOf(Count(id = "activity", hits = 94))
                )
            )
        )

        mockMvc.perform(get("/v1/videos?query=content").asTeacher(email = userAssignedToOrganisation().idOrThrow().value))
            .andExpect(status().isOk)
            .andExpect(header().string("Content-Type", "application/hal+json"))
            .andExpect(jsonPath("$._embedded.facets.resourceTypes.*", hasSize<Int>(1)))
            .andExpect(jsonPath("$._embedded.facets.resourceTypes.activity.hits", equalTo(94)))
    }

    @Test
    fun `resource_type_facets overwrite default resource type facets`() {
        mockMvc.perform(get("/v1/videos?query=content&resource_type_facets=Activity").asTeacher(email = userAssignedToOrganisation().idOrThrow().value))
            .andExpect(status().isOk)

        val lastSearchRequest = videoIndexFake.getLastSearchRequest()

        assertThat(lastSearchRequest.query.facetDefinition?.resourceTypes).containsExactly(
            "Activity"
        )
    }

    @Test
    fun `contains counts for video types`() {
        videoIndexFake.setFacets(
            listOf(
                FacetCount(
                    type = FacetType.VideoTypes,
                    counts = listOf(Count(id = "stock", hits = 94))
                )
            )
        )

        mockMvc.perform(get("/v1/videos?query=content").asTeacher(email = userAssignedToOrganisation().idOrThrow().value))
            .andExpect(status().isOk)
            .andExpect(header().string("Content-Type", "application/hal+json"))
            .andExpect(jsonPath("$._embedded.facets.videoTypes.*", hasSize<Int>(1)))
            .andExpect(jsonPath("$._embedded.facets.videoTypes.STOCK.hits", equalTo(94)))
    }

    @Test
    fun `video_type_facets overwrite default video type facets`() {
        mockMvc.perform(get("/v1/videos?query=content&video_type_facets=STOCK").asTeacher(email = userAssignedToOrganisation().idOrThrow().value))
            .andExpect(status().isOk)

        val lastSearchRequest = videoIndexFake.getLastSearchRequest()

        assertThat(lastSearchRequest.query.facetDefinition?.videoTypes).containsExactly(
            "STOCK"
        )
    }

    @Test
    fun `can request channel facets`() {
        val channel = saveChannel(name = "TED")
        val id = channel.id.value
        videoIndexFake.setFacets(
            listOf(
                FacetCount(
                    type = FacetType.Channels,
                    counts = listOf(Count(id = channel.id.value, hits = 94))
                )
            )
        )

        mockMvc.perform(get("/v1/videos?query=content&include_channel_facets=true").asTeacher(email = userAssignedToOrganisation().idOrThrow().value))
            .andExpect(status().isOk)
            .andExpect(header().string("Content-Type", "application/hal+json"))
            .andExpect(jsonPath("$._embedded.facets.channels.*", hasSize<Int>(1)))
            .andExpect(jsonPath("$._embedded.facets.channels.$id.id", equalTo(id)))
            .andExpect(jsonPath("$._embedded.facets.channels.$id.hits", equalTo(94)))
            .andExpect(jsonPath("$._embedded.facets.channels.$id.name", equalTo("TED")))
    }
}


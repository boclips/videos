package com.boclips.cleanser.domain.service

import com.boclips.cleanser.infrastructure.kaltura.client.KalturaClientException
import com.boclips.testsupport.AbstractSpringIntegrationTest
import com.boclips.testsupport.insert
import com.boclips.testsupport.loadFixture
import com.github.tomakehurst.wiremock.client.WireMock
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired

class VideoAnalysisServiceIntegrationTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var videoAnalysisService: VideoAnalysisService

    @Test
    fun getUnplayableVideos() {
        jdbcTemplate.update(insert(id = "10", title = "some unplayable video"))
        jdbcTemplate.update(insert(referenceId = "20", title = "another unplayable video"))
        jdbcTemplate.update(insert(id = "1", title = "some playable video"))

        wireMockServer.stubFor(WireMock.post(WireMock.urlEqualTo("/api_v3/service/media/action/list"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(loadFixture("two-successful-videos.json"))))

        val unplayableVideos = videoAnalysisService.getUnplayableVideos()

        assertThat(unplayableVideos).hasSize(2)
        assertThat(unplayableVideos).contains("10", "20")
    }

    @Test
    fun getPlayableVideos() {
        jdbcTemplate.update(insert(id = "1", title = "some playable video"))
        jdbcTemplate.update(insert(referenceId = "2", title = "some playable video"))

        wireMockServer.stubFor(WireMock.post(WireMock.urlEqualTo("/api_v3/service/media/action/list"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(loadFixture("two-successful-videos.json"))))

        val playableVideos = videoAnalysisService.getPlayableVideos()

        assertThat(playableVideos).hasSize(2)
        assertThat(playableVideos).contains("1", "2")
    }

    @Test
    fun getRemovableKalturaVideos() {
        wireMockServer.stubFor(WireMock.post(WireMock.urlEqualTo("/api_v3/service/media/action/list"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(loadFixture("two-successful-videos.json"))))

        val removableVideos = videoAnalysisService.getRemovableKalturaVideos()

        assertThat(removableVideos).hasSize(2)
        assertThat(removableVideos).contains("1", "2")
    }

    @Test
    fun canHandleConnectionDrops() {
        Assertions.assertThatThrownBy {
            videoAnalysisService.getFaultyVideosFromKaltura()
        }.isInstanceOf(KalturaClientException::class.java)
    }

}
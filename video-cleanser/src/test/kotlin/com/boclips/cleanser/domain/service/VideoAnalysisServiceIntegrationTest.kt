package com.boclips.cleanser.domain.service

import com.boclips.testsupport.AbstractSpringIntegrationTest
import com.boclips.testsupport.loadFixture
import com.github.tomakehurst.wiremock.client.WireMock
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired

class VideoAnalysisServiceIntegrationTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var videoAnalysisService: VideoAnalysisService

    @Test
    fun getUnplayableVideos() {
        addRow(id = "10", title = "some unplayable video")
        addRow(referenceId = "20", title = "another unplayable video")
        addRow(id = "1", title = "some playable video")

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
        addRow(id = "1", title = "some playable video")
        addRow(referenceId = "2", title = "some playable video")

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
    fun getFreeableVideos() {
        wireMockServer.stubFor(WireMock.post(WireMock.urlEqualTo("/api_v3/service/media/action/list"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(loadFixture("two-successful-videos.json"))))

        val freeableVideos = videoAnalysisService.getFreeableVideos()

        assertThat(freeableVideos).hasSize(2)
        assertThat(freeableVideos).contains("1", "2")
    }

    private fun addRow(id: String? = null, referenceId: String? = null, title: String? = "some title", contentProvider: String? = "some cp") {
        fun stringifyIfSet(string: String?): String? {
            if (string != null) return "'$string'"
            return null
        }

        jdbcTemplate.update("INSERT INTO metadata_orig(id, reference_id, title, source) " +
                "VALUES(${stringifyIfSet(id)}, ${stringifyIfSet(referenceId)}, ${stringifyIfSet(title)}, ${stringifyIfSet(contentProvider)})")
    }

}
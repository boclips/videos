package com.boclips.videoanalyser.domain.analysis.service

import com.boclips.videoanalyser.infrastructure.kaltura.client.KalturaClientException
import com.boclips.videoanalyser.testsupport.AbstractSpringIntegrationTest
import com.boclips.videoanalyser.testsupport.MetadataTestRepository
import com.boclips.videoanalyser.testsupport.loadFixture
import com.github.tomakehurst.wiremock.client.WireMock
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired

class VideoAnalysisServiceIntegrationTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var videoAnalysisService: VideoAnalysisService

    @Autowired
    lateinit var metadataTestRepository: MetadataTestRepository

    @Test
    fun getUnplayableVideos() {
        metadataTestRepository.insert(id = "10", title = "some unplayable video")
        metadataTestRepository.insert(referenceId = "20", title = "another unplayable video")
        metadataTestRepository.insert(id = "1", title = "some playable video")

        wireMockServer.stubFor(WireMock.post(WireMock.urlEqualTo("/api_v3/service/media/action/list"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(loadFixture("two-successful-videos.json"))))

        val unplayableVideos = videoAnalysisService.getUnplayableVideos()

        assertThat(unplayableVideos).hasSize(2)
        assertThat(unplayableVideos.map { it.kalturaReferenceId() }).contains("10", "20")
    }

    @Test
    fun getPlayableVideos() {
        metadataTestRepository.insert(id = "1", title = "some playable video")
        metadataTestRepository.insert(id = "27", referenceId = "2", title = "some playable video")

        wireMockServer.stubFor(WireMock.post(WireMock.urlEqualTo("/api_v3/service/media/action/list"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(loadFixture("two-successful-videos.json"))))

        val playableVideos = videoAnalysisService.getPlayableVideos()

        assertThat(playableVideos).hasSize(2)
        assertThat(playableVideos.map { it.kalturaReferenceId() }).contains("1", "2")
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
        assertThat(removableVideos.map { it.referenceId }).contains("1", "2")
    }

    @Test
    fun canHandleConnectionDrops() {
        Assertions.assertThatThrownBy {
            videoAnalysisService.getFaultyVideosFromKaltura()
        }.isInstanceOf(KalturaClientException::class.java)
    }

}
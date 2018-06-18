package com.boclips.cleanser.infrastructure.kaltura

import com.boclips.testsupport.AbstractSpringIntegrationTest
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired

class KalturaVideosRepositoryTest : AbstractSpringIntegrationTest() {
    val wireMockServer = WireMockServer(WireMockConfiguration.wireMockConfig().port(8089))

    @Autowired
    lateinit var kalturaVideosRepository: KalturaVideosRepository

    @Before
    fun startAndResetWireMock() {
        wireMockServer.start()
        wireMockServer.resetAll()
    }

    @After
    fun stopWireMock() {
        wireMockServer.stop()
    }

    @Test
    fun getAllNonErroredVideos_parsesMediaItems() {
        wireMockServer.stubFor(post(urlEqualTo("/api_v3/service/media/action/list"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(loadFixture("two-successful-videos.json"))))

        val allNonErroredVideoIds = kalturaVideosRepository.getAllIds()

        assertThat(allNonErroredVideoIds).containsExactly("1", "2")
    }

    private fun loadFixture(fileName: String) = Object::getClass.javaClass.classLoader.getResource(fileName).readBytes()
}
package com.boclips.cleanser.infrastructure.kaltura

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test

class KalturaClientTest {
    val wireMockServer = WireMockServer(WireMockConfiguration.wireMockConfig().port(8089))
    val kalturaClient = KalturaClient(KalturaProperties(host = "http://localhost:8089", session = "test-session"))

    @Before
    fun startAndResetWireMock() {
        wireMockServer.start()
        wireMockServer.resetAll()
        wireMockServer.stubFor(WireMock.post(WireMock.urlEqualTo("/api_v3/service/media/action/list"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(loadFixture("two-successful-videos.json"))))
    }

    @After
    fun stopWireMock() {
        wireMockServer.stop()
    }

    @Test
    fun fetch_returnsMediaItemsWithReferenceIds() {
        val mediaEntries = kalturaClient.fetchPagedMedia(500, 0)

        assertThat(mediaEntries).hasSize(2)
        assertThat(mediaEntries[0].referenceId).isEqualTo("1")
        assertThat(mediaEntries[1].referenceId).isEqualTo("2")
    }

    @Test
    fun fetch_pagination_respectsPageSize() {
        kalturaClient.fetchPagedMedia(pageSize = 100)

        wireMockServer.verify(1, WireMock
                .postRequestedFor(WireMock.urlEqualTo("/api_v3/service/media/action/list"))
                .withRequestBody(WireMock.containing("pager%5BpageSize%5D=100")))
    }

    @Test
    fun fetch_pagination_respectsCurrentPage() {
        kalturaClient.fetchPagedMedia(pageIndex = 2)

        wireMockServer.verify(1, WireMock
                .postRequestedFor(WireMock.urlEqualTo("/api_v3/service/media/action/list"))
                .withRequestBody(WireMock.containing("pager%5BpageIndex%5D=2")))
    }

    private fun loadFixture(fileName: String) = Object::getClass.javaClass.classLoader.getResource(fileName).readBytes()
}
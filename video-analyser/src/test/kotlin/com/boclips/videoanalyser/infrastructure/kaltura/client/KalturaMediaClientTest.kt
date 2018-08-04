package com.boclips.videoanalyser.infrastructure.kaltura.client

import com.boclips.videoanalyser.domain.model.MediaFilter
import com.boclips.videoanalyser.domain.model.MediaFilterType
import com.boclips.videoanalyser.testsupport.AbstractSpringIntegrationTest
import com.boclips.videoanalyser.testsupport.loadFixture
import com.github.tomakehurst.wiremock.client.WireMock
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired

class KalturaMediaClientTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var kalturaClient: KalturaMediaClient

    @Test
    fun fetch_mediaItem() {
        wireMockServer.stubFor(WireMock.post(WireMock.urlEqualTo("/api_v3/service/media/action/list"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(loadFixture("two-successful-videos.json"))))

        val mediaEntries = kalturaClient.fetch(500, 0)

        assertThat(mediaEntries).hasSize(2)

        assertThat(mediaEntries[0].referenceId).isEqualTo("1")
        assertThat(mediaEntries[0].id).isEqualTo("1_27l1ue65")

        assertThat(mediaEntries[1].referenceId).isEqualTo("2")
        assertThat(mediaEntries[1].id).isEqualTo("1_antpp8un")
    }

    @Test
    fun fetch_respectsPageSize() {
        wireMockServer.stubFor(WireMock.post(WireMock.urlEqualTo("/api_v3/service/media/action/list"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(loadFixture("two-successful-videos.json"))))

        kalturaClient.fetch(pageSize = 100)

        wireMockServer.verify(1, WireMock
                .postRequestedFor(WireMock.urlEqualTo("/api_v3/service/media/action/list"))
                .withRequestBody(WireMock.containing("pager%5BpageSize%5D=100")))
    }

    @Test
    fun fetch_respectsPageIndex() {
        wireMockServer.stubFor(WireMock.post(WireMock.urlEqualTo("/api_v3/service/media/action/list"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(loadFixture("two-successful-videos.json"))))

        kalturaClient.fetch(pageIndex = 2)

        wireMockServer.verify(1, WireMock
                .postRequestedFor(WireMock.urlEqualTo("/api_v3/service/media/action/list"))
                .withRequestBody(WireMock.containing("pager%5BpageIndex%5D=2")))
    }

    @Test
    fun fetch_takesOptionalFilters() {
        wireMockServer.stubFor(WireMock.post(WireMock.urlEqualTo("/api_v3/service/media/action/list"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(loadFixture("two-successful-videos.json"))))

        val filters = listOf(
                MediaFilter(MediaFilterType.CREATED_AT_LESS_THAN_OR_EQUAL, "173131231"),
                MediaFilter(MediaFilterType.CREATED_AT_GREATER_THAN_OR_EQUAL, "123131231"),
                MediaFilter(MediaFilterType.STATUS_IN, "2,-2")
        )

        kalturaClient.fetch(filters = filters)

        wireMockServer.verify(1, WireMock
                .postRequestedFor(WireMock.urlEqualTo("/api_v3/service/media/action/list"))
                .withRequestBody(WireMock.containing("filter%5BcreatedAtLessThanOrEqual%5D=173131231"))
                .withRequestBody(WireMock.containing("filter%5BcreatedAtGreaterThanOrEqual%5D=123131231"))
                .withRequestBody(WireMock.containing("filter%5BstatusIn%5D=2%2C-2"))
        )
    }

    @Test
    fun count_returnsCountOfEntireCollection() {
        wireMockServer.stubFor(WireMock.post(WireMock.urlEqualTo("/api_v3/service/media/action/list"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(loadFixture("two-successful-videos.json"))))

        val count = kalturaClient.count(emptyList())

        assertThat(count).isEqualTo(2L)
    }
}
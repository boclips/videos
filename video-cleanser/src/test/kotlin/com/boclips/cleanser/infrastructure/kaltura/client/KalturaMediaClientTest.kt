package com.boclips.cleanser.infrastructure.kaltura.client

import com.boclips.cleanser.domain.model.MediaFilter
import com.boclips.cleanser.domain.model.MediaFilterType
import com.boclips.cleanser.infrastructure.kaltura.KalturaProperties
import com.boclips.testsupport.AbstractWireMockTest
import com.boclips.testsupport.loadFixture
import com.github.tomakehurst.wiremock.client.WireMock
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Before
import org.junit.Test

class KalturaMediaClientTest : AbstractWireMockTest() {
    private val kalturaClient = KalturaMediaClient(KalturaProperties(
            host = "http://localhost:${AbstractWireMockTest.PORT}",
            session = "test-session"))

    @Before
    fun setUp() {
        wireMockServer.stubFor(WireMock.post(WireMock.urlEqualTo("/api_v3/service/media/action/list"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(loadFixture("two-successful-videos.json"))))
    }

    @Test
    fun fetch_returnsMediaItemsWithReferenceIds() {
        val mediaEntries = kalturaClient.fetch(500, 0)

        assertThat(mediaEntries).hasSize(2)
        assertThat(mediaEntries[0].referenceId).isEqualTo("1")
        assertThat(mediaEntries[1].referenceId).isEqualTo("2")
    }

    @Test
    fun fetch_returnsMediaItemsWithIds() {
        val mediaEntries = kalturaClient.fetch(500, 0)

        assertThat(mediaEntries[0].id).isEqualTo("1_27l1ue65")
        assertThat(mediaEntries[1].id).isEqualTo("1_antpp8un")
    }

    @Test
    fun fetch_pagination_respectsPageSize() {
        kalturaClient.fetch(pageSize = 100)

        wireMockServer.verify(1, WireMock
                .postRequestedFor(WireMock.urlEqualTo("/api_v3/service/media/action/list"))
                .withRequestBody(WireMock.containing("pager%5BpageSize%5D=100")))
    }

    @Test
    fun fetch_pagination_respectsCurrentPage() {
        kalturaClient.fetch(pageIndex = 2)

        wireMockServer.verify(1, WireMock
                .postRequestedFor(WireMock.urlEqualTo("/api_v3/service/media/action/list"))
                .withRequestBody(WireMock.containing("pager%5BpageIndex%5D=2")))
    }

    @Test
    fun fetch_throwsIfSomethingUnexpectedWentWrong() {
        wireMockServer.resetAll()
        wireMockServer.stubFor(WireMock.post(WireMock.urlEqualTo("/api_v3/service/media/action/list"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("something went wrong")))

        assertThatThrownBy { kalturaClient.fetch() }.isInstanceOf(KalturaClientException::class.java)
    }

    @Test
    fun fetch_abortsIfBodyIsEmpty() {
        wireMockServer.resetAll()
        wireMockServer.stubFor(WireMock.post(WireMock.urlEqualTo("/api_v3/service/media/action/list"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                ))

        assertThatThrownBy { kalturaClient.fetch() }.isInstanceOf(KalturaClientException::class.java)
    }

    @Test
    fun fetch_canCopeWithEmptyObjects() {
        wireMockServer.resetAll()
        wireMockServer.stubFor(WireMock.post(WireMock.urlEqualTo("/api_v3/service/media/action/list"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(loadFixture("empty-objects.json"))))

        val mediaItems = kalturaClient.fetch()

        assertThat(mediaItems).hasSize(0)
    }

    @Test
    fun fetch_takesOptionalFilters() {
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
        val count = kalturaClient.count(emptyList())

        assertThat(count).isEqualTo(2L)
    }
}
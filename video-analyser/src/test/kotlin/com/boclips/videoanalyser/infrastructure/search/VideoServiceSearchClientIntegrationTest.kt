package com.boclips.videoanalyser.infrastructure.search

import com.boclips.videoanalyser.testsupport.AbstractSpringIntegrationTest
import com.boclips.videoanalyser.testsupport.loadFixture
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

import org.springframework.beans.factory.annotation.Autowired

class VideoServiceSearchClientIntegrationTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var searchClient: VideoServiceSearchClient

    @Test
    fun search() {
        wireMockServer.stubFor(WireMock.get(WireMock.urlEqualTo("/videos/search?query=elephants"))
                .withHeader("Authorization", equalTo("Basic dXNlcjpwYXNzd29yZA=="))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(loadFixture("video-service-search-response.json"))))

        val searchResults = searchClient.searchTop10("elephants").toList()

        assertThat(searchResults).containsExactly("1661551", "1665344", "1661324", "1661712", "1661724", "1665702", "1661304", "1665295", "1661325", "1661306")
    }

    @Test
    fun `search when query has whitespaces`() {
        wireMockServer.stubFor(WireMock.get(WireMock.urlEqualTo("/videos/search?query=elephant%20habits"))
                .withHeader("Authorization", equalTo("Basic dXNlcjpwYXNzd29yZA=="))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(loadFixture("video-service-search-response.json"))))

        val searchResults = searchClient.searchTop10("elephant habits").toList()

        assertThat(searchResults).contains("1661551")
    }
}

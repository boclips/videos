package com.boclips.videoanalyser.infrastructure.boclips

import com.boclips.videoanalyser.testsupport.AbstractSpringIntegrationTest
import com.boclips.videoanalyser.testsupport.loadFixture
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

import org.springframework.beans.factory.annotation.Autowired

class LegacyBoclipsSearchClientIntegrationTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var searchClient: LegacyBoclipsSearchClient

    @Test
    fun search() {
        wireMockServer.stubFor(WireMock.get(WireMock.urlEqualTo("/search?q=_text_:(enzyme)"))
                .withHeader("Authorization", equalTo("Bearer testboclipstoken"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(loadFixture("legacy-solr-response.json"))))

        val searchResults = searchClient.searchTop10("enzyme").toList()

        assertThat(searchResults).containsExactly("2499562", "2476304", "2499583", "2500221", "2500222", "2476290", "1524040", "2539387", "2539417", "1647267")
    }
    @Test
    fun search_whenSpaceInQuery() {
        wireMockServer.stubFor(WireMock.get(WireMock.urlEqualTo("/search?q=_text_:(enzyme%20avoriaz)"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(loadFixture("legacy-solr-response.json"))))

        val searchResults = searchClient.searchTop10("enzyme avoriaz").toList()

        assertThat(searchResults).hasSize(10)
    }
}

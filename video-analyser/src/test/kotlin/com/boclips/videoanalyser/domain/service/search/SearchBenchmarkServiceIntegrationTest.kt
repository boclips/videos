package com.boclips.videoanalyser.domain.service.search

import com.boclips.videoanalyser.domain.model.search.SearchExpectation
import com.boclips.videoanalyser.testsupport.AbstractSpringIntegrationTest
import com.boclips.videoanalyser.testsupport.loadFixture
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired

class SearchBenchmarkServiceIntegrationTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var searchBenchmarkService: SearchBenchmarkService

    @Test
    fun benchmark() {
        wireMockServer.stubFor(get(urlEqualTo("/search?q=_text_:(enzyme)"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(loadFixture("legacy-solr-response.json"))))

        wireMockServer.stubFor(WireMock.get(WireMock.urlEqualTo("/videos/search?query=enzyme"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(loadFixture("video-service-search-response.json"))))

        val searchExpectation = SearchExpectation("enzyme", "2499583")

        val (item) = searchBenchmarkService.benchmark(listOf(searchExpectation))

        assertThat(item.expectation).isEqualTo(searchExpectation)
        assertThat(item.legacySearchHit).isEqualTo(true)
        assertThat(item.videoServiceHit).isEqualTo(false)
    }
}

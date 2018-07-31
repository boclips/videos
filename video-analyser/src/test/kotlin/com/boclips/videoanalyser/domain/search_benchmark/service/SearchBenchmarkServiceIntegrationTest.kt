package com.boclips.videoanalyser.domain.search_benchmark.service

import com.boclips.videoanalyser.domain.search_benchmark.service.SearchBenchmarkService
import com.boclips.videoanalyser.domain.search_benchmark.domain.SearchExpectation
import com.boclips.videoanalyser.testsupport.AbstractSpringIntegrationTest
import com.boclips.videoanalyser.testsupport.loadFixture
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

        val searchExpectation = SearchExpectation("enzyme", "2499583")

        val (total, hits) = searchBenchmarkService.benchmark(listOf(searchExpectation))

        assertThat(total).isEqualTo(1)
        assertThat(hits).isEqualTo(1)
    }
}

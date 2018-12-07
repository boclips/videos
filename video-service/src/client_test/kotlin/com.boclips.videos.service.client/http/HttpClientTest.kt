package com.boclips.videos.service.client.http

import com.boclips.videos.service.client.exceptions.ResourceForbiddenException
import com.github.kittinunf.fuel.Fuel
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class HttpClientTest {

    @Test
    fun `makeRequest throws an exception when response status forbidden`() {

        val wireMockServer = WireMockServer(WireMockConfiguration.wireMockConfig().port(8089))
        configureFor("localhost", 8089)
        wireMockServer.start()

        stubFor(get("/some-url").willReturn(aResponse().withStatus(403)))

        val request = Fuel.get("http://localhost:8089/some-url")

        assertThatThrownBy {
            HttpClient.makeRequest(request)
        }
                .isInstanceOf(ResourceForbiddenException::class.java)
                .hasMessage("http://localhost:8089/some-url")
    }
}

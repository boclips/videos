package com.boclips.videos.service.client.http

import com.boclips.videos.service.client.exceptions.ClientErrorException
import com.boclips.videos.service.client.exceptions.ResourceForbiddenException
import com.boclips.videos.service.client.exceptions.ResourceNotFoundException
import com.boclips.videos.service.client.exceptions.ServerErrorException
import com.github.kittinunf.fuel.Fuel
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class HttpClientTest {

    lateinit var wireMockServer: WireMockServer

    @BeforeEach
    fun setUp() {
        wireMockServer = WireMockServer(WireMockConfiguration.wireMockConfig().port(8089))
        configureFor("localhost", 8089)
        wireMockServer.start()
    }

    @AfterEach
    fun tearDown() {
        wireMockServer.stop()
    }

    @Test
    fun `makeRequest throws an exception when response status forbidden`() {
        stubFor(get("/some-url").willReturn(aResponse().withStatus(403)))

        val request = Fuel.get("http://localhost:8089/some-url")

        assertThatThrownBy {
            HttpClient.makeRequest(request)
        }
                .isInstanceOf(ResourceForbiddenException::class.java)
                .hasMessage("http://localhost:8089/some-url")
    }

    @Test
    fun `makeRequest throws an exception when response status not found`() {
        stubFor(get("/some-url").willReturn(aResponse().withStatus(404)))

        val request = Fuel.get("http://localhost:8089/some-url")

        assertThatThrownBy {
            HttpClient.makeRequest(request)
        }
                .isInstanceOf(ResourceNotFoundException::class.java)
                .hasMessage("http://localhost:8089/some-url")
    }

    @Test
    fun `makeRequest throws an exception when response status indicates server error`() {
        stubFor(get("/some-url").willReturn(aResponse().withStatus(500)))

        val request = Fuel.get("http://localhost:8089/some-url")

        assertThatThrownBy {
            HttpClient.makeRequest(request)
        }
                .isInstanceOf(ServerErrorException::class.java)
    }

    @Test
    fun `makeRequest throws an exception when response status indicates client error`() {
        stubFor(get("/some-url").willReturn(aResponse().withStatus(400)))

        val request = Fuel.get("http://localhost:8089/some-url")

        assertThatThrownBy {
            HttpClient.makeRequest(request)
        }
                .isInstanceOf(ClientErrorException::class.java)
    }
}

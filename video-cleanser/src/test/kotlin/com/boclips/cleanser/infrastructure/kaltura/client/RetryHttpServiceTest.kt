package com.boclips.cleanser.infrastructure.kaltura.client

import com.boclips.testsupport.AbstractSpringIntegrationTest
import com.github.tomakehurst.wiremock.client.WireMock
import org.assertj.core.api.Assertions
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpEntity
import java.net.URI

class RetryHttpServiceTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var retryHttpServiceTest: RetryHttpService

    @Test
    fun post_retriesAndThenThrows() {
        wireMockServer.stubFor(WireMock.post(WireMock.urlEqualTo("/api_v3/service/media/action/list"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("something went wrong")))

        Assertions.assertThatThrownBy {
            retryHttpServiceTest.post(
                    HttpEntity(null, null),
                    URI("http://localhost:$PORT/api_v3/service/media/action/list")
            )
        }.isInstanceOf(Exception::class.java)
    }
}
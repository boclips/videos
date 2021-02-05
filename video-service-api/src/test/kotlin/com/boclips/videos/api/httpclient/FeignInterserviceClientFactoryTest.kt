package com.boclips.videos.api.httpclient

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import feign.Param
import feign.RequestLine
import feign.okhttp.OkHttpClient
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import ru.lanwen.wiremock.ext.WiremockResolver
import ru.lanwen.wiremock.ext.WiremockUriResolver

@ExtendWith(
    value = [
        WiremockResolver::class,
        WiremockUriResolver::class
    ]
)
class FeignInterserviceClientFactoryTest {

    @Test
    fun `should create fully fledged Feign HTTP client`(
        @WiremockResolver.Wiremock server: WireMockServer,
        @WiremockUriResolver.WiremockUri uri: String
    ) {
        val clientInterface = FeignInterserviceClientFactory.create(
            uri,
            feignClient = OkHttpClient(),
            clientInterface = TheClientInterface::class.java
        )

        server.stubFor(
            WireMock.get(WireMock.urlEqualTo("/resource/the-ID"))
                .willReturn(
                    WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""{ "someKey" : "the actual value" }""".trimIndent())
                )
        )

        clientInterface.getSomething("the-ID")
    }

    private interface TheClientInterface {

        @RequestLine("GET /resource/{aPathParam}")
        fun getSomething(
            @Param("aPathParam") aPathParam: String
        ): TheResource
    }

    private data class TheResource(val someKey: String, val optionalField: String?)
}

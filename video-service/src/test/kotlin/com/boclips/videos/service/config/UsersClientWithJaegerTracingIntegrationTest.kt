package com.boclips.videos.service.config

import com.boclips.users.api.httpclient.UsersClient
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import feign.okhttp.OkHttpClient
import feign.opentracing.TracingClient
import io.jaegertracing.internal.JaegerTracer
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import ru.lanwen.wiremock.ext.WiremockResolver
import ru.lanwen.wiremock.ext.WiremockResolver.Wiremock
import ru.lanwen.wiremock.ext.WiremockUriResolver
import ru.lanwen.wiremock.ext.WiremockUriResolver.WiremockUri

@ExtendWith(
    value = [
        WiremockResolver::class,
        WiremockUriResolver::class
    ]
)
class UsersClientWithJaegerTracingIntegrationTest {

    @Test
    fun `uber-trace-is header should be sent when jaeger tracer is present in the context`(
        @Wiremock server: WireMockServer,
        @WiremockUri uri: String
    ) {
        val usersClient = buildUsersClientWithJaegerTracing(uri)

        val userId = "someId"

        server.stubFor(
            get(urlEqualTo("/v1/users/$userId/access-rules"))
                .withHeader("uber-trace-id", matching(".*%3A.*"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""{"_embedded": { "accessRules" : [] }}""".trimIndent())
                )
        )

        usersClient.getAccessRulesOfUser(userId)
    }

    private fun buildUsersClientWithJaegerTracing(uri: String): UsersClient {
        val rawFeignClient = OkHttpClient()
        val jaegerTracer = JaegerTracer.Builder("serviceName").build()
        val decoratedFeignClient = TracingClient(rawFeignClient, jaegerTracer)
        return UsersClient.create(
            apiUrl = uri,
            feignClient = decoratedFeignClient
        )
    }
}

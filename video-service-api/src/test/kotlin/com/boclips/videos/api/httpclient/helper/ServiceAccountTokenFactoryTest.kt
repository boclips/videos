package com.boclips.videos.api.httpclient.helper

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import org.assertj.core.api.Assertions.assertThat
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
class ServiceAccountTokenFactoryTest {
    @Test
    fun `obtains a fresh token`(@WiremockResolver.Wiremock server: WireMockServer, @WiremockUriResolver.WiremockUri uri: String) {
        stubTokenResponse(server = server, token = "token-123", expiry = 300)

        val serviceAccountTokenFactory = ServiceAccountTokenFactory(
            ServiceAccountCredentials(
                authEndpoint = uri,
                clientId = "clientId",
                clientSecret = "secret"
            )
        )

        val accessToken = serviceAccountTokenFactory.getAccessToken()

        assertThat(accessToken).isEqualTo("token-123")
    }

    @Test
    fun `refreshes token after expiry`(@WiremockResolver.Wiremock server: WireMockServer, @WiremockUriResolver.WiremockUri uri: String) {
        stubTokenResponse(server = server, token = "token-123", expiry = -1)

        val serviceAccountTokenFactory = ServiceAccountTokenFactory(
            ServiceAccountCredentials(
                authEndpoint = uri,
                clientId = "clientId",
                clientSecret = "secret"
            )
        )

        assertThat(serviceAccountTokenFactory.getAccessToken()).isEqualTo("token-123")
        stubTokenResponse(server = server, token = "token-456")

        assertThat(serviceAccountTokenFactory.getAccessToken()).isEqualTo("token-456")
    }

    private fun stubTokenResponse(server: WireMockServer, token: String = "token-123", expiry: Int = 300) {
        server
            .stubFor(
                post(urlEqualTo("/v1/token"))
                    .withHeader("Content-Type", equalTo("application/x-www-form-urlencoded; charset=UTF-8"))
                    .withBasicAuth("clientId", "secret")
                    .willReturn(
                        aResponse()
                            .withStatus(200)
                            .withHeader("Content-Type", "application/json")
                            .withBody(
                                """
                                {
                                  "access_token": "$token",
                                  "expires_in": $expiry,
                                  "refresh_expires_in": 604800,
                                  "refresh_token": "refresh-token",
                                  "token_type": "bearer",
                                  "not-before-policy": 1578062983,
                                  "session_state": "f9e8b47b-8a16-4950-b529-f4e23b12e809",
                                  "scope": "profile email"
                                }
                            """.trimIndent()
                            )
                    )
            )
    }
}

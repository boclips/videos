package com.boclips.videos.api.httpclient.helper

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import feign.Feign
import feign.Headers
import feign.Logger
import feign.RequestLine
import feign.form.FormEncoder
import feign.jackson.JacksonDecoder
import feign.okhttp.OkHttpClient
import feign.slf4j.Slf4jLogger
import java.util.Base64

interface KeycloakClient {
    @RequestLine("POST /v1/token")
    @Headers("Content-Type: application/x-www-form-urlencoded")
    fun getToken(tokenRequest: TokenRequest): TokenResponse

    companion object {
        fun create(
            credentials: ServiceAccountCredentials
        ): KeycloakClient {
            val objectMapper = ObjectMapper().apply {
                this.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            }

            return Feign.builder()
                .client(OkHttpClient())
                .encoder(FormEncoder())
                .decoder(JacksonDecoder(objectMapper))
                .requestInterceptor { template ->
                    val secret = Base64.getEncoder()
                        .encodeToString("${credentials.clientId}:${credentials.clientSecret}".toByteArray())
                    template.header("Authorization", "Basic $secret")
                }
                .logLevel(Logger.Level.FULL)
                .logger(Slf4jLogger())
                .target(KeycloakClient::class.java, credentials.authEndpoint)
        }
    }
}

data class TokenResponse(var access_token: String? = null, var expires_in: Int? = null)
data class TokenRequest(var grant_type: String? = "client_credentials")
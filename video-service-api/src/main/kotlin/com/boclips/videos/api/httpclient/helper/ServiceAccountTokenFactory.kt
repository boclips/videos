package com.boclips.videos.api.httpclient.helper

import java.time.Duration
import java.time.Instant

class ServiceAccountTokenFactory(serviceAccountCredentials: ServiceAccountCredentials) :
    TokenFactory {
    private val client: KeycloakClient = KeycloakClient.create(serviceAccountCredentials)
    private lateinit var lastRefreshed: Instant
    private lateinit var currentToken: TokenResponse

    init {
        refreshToken()
    }

    override fun getAccessToken(): String {
        val timeElapsed: Long = Duration.between(lastRefreshed, Instant.now()).toMillis()
        if (timeElapsed > currentToken.expires_in) {
            refreshToken()
        }

        return currentToken.access_token
    }

    private fun refreshToken() {
        currentToken = client.getToken(TokenRequest())
        lastRefreshed = Instant.now()
    }
}
package com.boclips.videos.api.httpclient.helper

import java.time.Duration
import java.time.Instant

class ServiceAccountTokenFactory(serviceAccountCredentials: ServiceAccountCredentials) :
    TokenFactory {
    private val client: KeycloakClient = KeycloakClient.create(serviceAccountCredentials)
    private var lastRefreshTime: Instant? = null
    private lateinit var currentToken: TokenResponse

    override fun getAccessToken(): String {
        if (shouldRefreshToken()) {
            refreshToken()
        }

        return currentToken.access_token!!
    }

    @Synchronized
    private fun shouldRefreshToken(): Boolean {
        if (lastRefreshTime == null) {
            return true
        }

        return hasTokenExpired(lastRefreshTime!!)
    }

    private fun hasTokenExpired(previousRefreshTime: Instant): Boolean {
        val timeElapsed: Long = Duration.between(previousRefreshTime, Instant.now()).seconds
        return timeElapsed > currentToken.expires_in!!
    }

    private fun refreshToken() {
        currentToken = client.getToken(TokenRequest())
        lastRefreshTime = Instant.now()
    }
}

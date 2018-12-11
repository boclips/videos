package com.boclips.videos.service.client

import com.boclips.videos.service.client.internal.ApiClient
import com.boclips.videos.service.client.internal.FakeClient
import org.springframework.security.oauth2.client.OAuth2RestTemplate
import org.springframework.security.oauth2.client.token.grant.client.ClientCredentialsResourceDetails
import org.springframework.web.client.RestTemplate

interface VideoServiceClient {

    fun create(request: CreateVideoRequest)
    fun existsByContentPartnerInfo(contentPartnerId: String, contentPartnerVideoId: String): Boolean

    companion object {
        fun getFakeClient() : VideoServiceClient {
            return FakeClient()
        }

        fun getApiClient(baseUrl: String, serviceCredentials: ServiceCredentials) : VideoServiceClient {
            val restTemplate = OAuth2RestTemplate( ClientCredentialsResourceDetails().apply {
                accessTokenUri = serviceCredentials.accessTokenUri
                clientId = serviceCredentials.clientId
                clientSecret = serviceCredentials.clientSecret
            } )
            return ApiClient(baseUrl, restTemplate)
        }

        fun getUnauthorisedApiClient(baseUrl: String) : VideoServiceClient {
            return ApiClient(baseUrl, RestTemplate())
        }
    }
}

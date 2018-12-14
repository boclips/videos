package com.boclips.videos.service.client

import com.boclips.videos.service.client.internal.ApiClient
import com.boclips.videos.service.client.internal.FakeClient
import com.boclips.videos.service.client.spring.Video
import org.springframework.security.oauth2.client.OAuth2RestTemplate
import org.springframework.security.oauth2.client.token.grant.client.ClientCredentialsResourceDetails
import org.springframework.web.client.RestTemplate

interface VideoServiceClient {

    fun create(request: CreateVideoRequest): VideoId
    fun existsByContentPartnerInfo(contentPartnerId: String, contentPartnerVideoId: String): Boolean
    fun setSubjects(id: VideoId, subjects: Set<String>)
    fun get(id: VideoId) : Video
}

class VideoServiceClientFactory {
    companion object {
        @JvmStatic
        fun getFakeClient() : FakeClient {
            return FakeClient()
        }

        @JvmStatic
        fun getApiClient(baseUrl: String, serviceCredentials: ServiceCredentials) : VideoServiceClient {
            val restTemplate = OAuth2RestTemplate( ClientCredentialsResourceDetails().apply {
                accessTokenUri = serviceCredentials.accessTokenUri
                clientId = serviceCredentials.clientId
                clientSecret = serviceCredentials.clientSecret
            } )
            return ApiClient(baseUrl, restTemplate)
        }

        @JvmStatic
        fun getUnauthorisedApiClient(baseUrl: String) : VideoServiceClient {
            return ApiClient(baseUrl, RestTemplate())
        }
    }
}

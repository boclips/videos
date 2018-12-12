package com.boclips.videos.service.client.internal

import com.boclips.videos.service.client.CreateVideoRequest
import com.boclips.videos.service.client.VideoServiceClient
import org.springframework.http.HttpStatus
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate

internal class ApiClient internal constructor(
        private val baseUrl: String,
        private val restTemplate: RestTemplate
) : VideoServiceClient {
    override fun create(request: CreateVideoRequest) {
        restTemplate.postForObject("$baseUrl/v1/videos", request, String::class.java)
    }

    override fun existsByContentPartnerInfo(contentPartnerId: String, contentPartnerVideoId: String) = try {
        restTemplate.headForHeaders("$baseUrl/v1/content-partners/$contentPartnerId/videos/$contentPartnerVideoId")
        true
    } catch (e: HttpClientErrorException) {
        if (e.statusCode == HttpStatus.NOT_FOUND) {
            false
        } else {
            throw e
        }
    }
}


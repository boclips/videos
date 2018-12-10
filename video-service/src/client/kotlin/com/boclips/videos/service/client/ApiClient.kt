package com.boclips.videos.service.client

import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.postForObject

class ApiClient(private val baseUrl: String) : VideoServiceClient {

    private val restTemplate = RestTemplate()

    override fun create(request: CreateVideoRequest) {
        restTemplate.postForObject<String>("$baseUrl/v1/videos", request)
    }

    override fun existsByContentPartnerInfo(contentPartnerId: String, contentPartnerVideoId: String) = try {
        restTemplate.headForHeaders("$baseUrl/v1/content_partners/$contentPartnerId/partner_video_id/$contentPartnerVideoId")
        true
    } catch (e: HttpClientErrorException) {
        false
    }
}

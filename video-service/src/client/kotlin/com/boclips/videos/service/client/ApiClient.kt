package com.boclips.videos.service.client

import com.boclips.videos.service.client.exceptions.ResourceNotFoundException
import com.boclips.videos.service.client.http.HttpClient
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.kittinunf.fuel.Fuel

class ApiClient(private val baseUrl: String) : VideoServiceClient {

    override fun create(request: CreateVideoRequest) {
        val mapper = ObjectMapper().findAndRegisterModules()
        val body = mapper.writeValueAsString(request)
        HttpClient.makeRequest(Fuel.post("$baseUrl/v1/videos").header("content-type" to "application/json").body(body))
    }

    override fun existsByContentPartnerInfo(contentPartnerId: String, contentPartnerVideoId: String) = try {
        HttpClient.makeRequest(
                Fuel.head("${this.baseUrl}/v1/content_partners/$contentPartnerId/partner_video_id/$contentPartnerVideoId")
        ).statusCode == 200
    } catch (e: ResourceNotFoundException) {
        false
    }
}

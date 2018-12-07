package com.boclips.videos.service.client

import com.boclips.videos.service.client.http.HttpClient
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.github.kittinunf.fuel.Fuel

class ApiClient(private val baseUrl: String) : VideoServiceClient {

    override fun create(request: CreateVideoRequest) {
        val mapper = ObjectMapper().registerModule(KotlinModule())
        val body = mapper.writeValueAsString(request)
        HttpClient.makeRequest(Fuel.post("$baseUrl/v1/videos").body(body))
    }

    override fun existsByContentPartnerInfo(contentPartnerId: String, contentPartnerVideoId: String): Boolean {
        val response = HttpClient.makeRequest(
                Fuel.head("${this.baseUrl}/v1/content_partners/$contentPartnerId/partner_video_id/$contentPartnerVideoId")
        )
        return response.statusCode == 200
    }
}

package com.boclips.videos.service.client.internal

import com.boclips.videos.service.client.CreateVideoRequest
import com.boclips.videos.service.client.VideoId
import com.boclips.videos.service.client.VideoServiceClient
import com.boclips.videos.service.client.spring.Video
import org.springframework.http.HttpStatus
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate

internal class ApiClient internal constructor(
        private val baseUrl: String,
        private val restTemplate: RestTemplate
) : VideoServiceClient {

    override fun create(request: CreateVideoRequest): VideoId {
        val uri = restTemplate.postForLocation("$baseUrl/v1/videos", request, String::class.java)
        return VideoId(uri = uri)
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

    override fun setSubjects(id: VideoId, subjects: Set<String>) {
        val body = mapOf("subjects" to subjects)
        restTemplate.postForObject(id.uri, body, String::class.java)
    }

    override fun get(id: VideoId): Video {
        return restTemplate.getForObject(id.uri, VideoResource::class.java).toVideo()
    }
}



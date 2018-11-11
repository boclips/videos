package com.boclips.videoanalyser.infrastructure.search

import com.boclips.videoanalyser.domain.service.search.SearchClient
import com.jayway.jsonpath.JsonPath
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.stereotype.Component
import org.springframework.web.client.getForObject

@Component
class VideoServiceSearchClient(
        restTemplateBuilder: RestTemplateBuilder,
        videoServiceSearchProperties: VideoServiceSearchProperties
) : SearchClient {

    override fun serviceName(): String {
        return "asset-service"
    }

    val restTemplate = videoServiceSearchProperties.validate().let {
        restTemplateBuilder
                .rootUri(videoServiceSearchProperties.baseUrl)
                .basicAuthorization(videoServiceSearchProperties.username, videoServiceSearchProperties.password)
                .build()!!
    }

    override fun searchTop10(query: String): Iterable<String> {
        val output = restTemplate.getForObject<String>("/videos/search?query=$query")
        val ids: List<String> = JsonPath.read(output, "$.videos.[*].id")
        return ids.take(10)
    }
}
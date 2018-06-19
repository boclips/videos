package com.boclips.cleanser.infrastructure.kaltura

import com.boclips.cleanser.infrastructure.kaltura.response.MediaItem
import com.boclips.cleanser.infrastructure.kaltura.response.MediaList
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.RestTemplate
import java.net.URI


@Component
class KalturaClient(val kalturaProperties: KalturaProperties) {
    private val MEDIA_ENDPOINT = "/api_v3/service/media/action/list"
    private val restTemplate = RestTemplate()

    fun fetch(): List<MediaItem> {
        val request = HttpEntity<MultiValueMap<String, String>>(buildData(), buildHeaders())

        val response = restTemplate.postForEntity(
                URI("${kalturaProperties.host}$MEDIA_ENDPOINT"),
                request,
                MediaList::class.java)

        return response.body?.let { it.items }.orEmpty()
    }

    private fun buildData(): LinkedMultiValueMap<String, String> {
        val map = LinkedMultiValueMap<String, String>()
        map.add("format", "1")
        map.add("ks", kalturaProperties.session)
        return map
    }

    private fun buildHeaders(): HttpHeaders {
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_FORM_URLENCODED
        headers.accept = mutableListOf(MediaType.ALL)
        return headers
    }
}

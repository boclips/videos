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

    fun fetchPagedMedia(pageSize: Int = 500, pageIndex: Int = 0): List<MediaItem> {
        val request = HttpEntity<MultiValueMap<String, String>>(buildData(pageSize, pageIndex), buildHeaders())

        val response = restTemplate.postForEntity(
                URI("${kalturaProperties.host}$MEDIA_ENDPOINT"),
                request,
                MediaList::class.java)

        return response.body?.let { it.items }.orEmpty()
    }

    private fun buildData(pageSize: Int, currentPage: Int): LinkedMultiValueMap<String, String> {
        val map = LinkedMultiValueMap<String, String>()
        map.add("format", "1")
        map.add("ks", kalturaProperties.session)
        map.add("pager[pageSize]", pageSize.toString())
        map.add("pager[pageIndex]", currentPage.toString())
        return map
    }

    private fun buildHeaders(): HttpHeaders {
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_FORM_URLENCODED
        headers.accept = mutableListOf(MediaType.ALL)
        return headers
    }
}

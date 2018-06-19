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
class KalturaMediaClient(val kalturaProperties: KalturaProperties) {
    private val ENDPOINT = "/api_v3/service/media/action/list"
    private val restTemplate = RestTemplate()

    fun count(): Long {
        val request = HttpEntity<MultiValueMap<String, String>>(buildRequestBody(pageSize = 1, pageIndex = 0), buildHeaders())
        return post(request).count
    }

    fun fetch(pageSize: Int = 500, pageIndex: Int = 0): List<MediaItem> {
        val request = HttpEntity<MultiValueMap<String, String>>(buildRequestBody(pageSize, pageIndex), buildHeaders())
        return post(request).items
    }

    private fun post(request: HttpEntity<MultiValueMap<String, String>>): MediaList {
        try {
            val response = restTemplate.postForEntity(
                    URI("${kalturaProperties.host}$ENDPOINT"),
                    request,
                    MediaList::class.java)
            if (response.body == null) throw IllegalStateException("Received a response without a body") else return response.body!!
        } catch (ex: Exception) {
            throw KalturaClientException(ex)
        }
    }

    private fun buildRequestBody(pageSize: Int?, pageIndex: Int?): LinkedMultiValueMap<String, String> {
        val params = LinkedMultiValueMap<String, String>()
        params.add("format", "1")
        params.add("ks", kalturaProperties.session)

        if (pageSize != null) params.add("pager[pageSize]", pageSize.toString())
        if (pageIndex != null) params.add("pager[pageIndex]", pageIndex.toString())

        return params
    }

    private fun buildHeaders(): HttpHeaders {
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_FORM_URLENCODED
        headers.accept = mutableListOf(MediaType.ALL)
        return headers
    }
}

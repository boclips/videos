package com.boclips.cleanser.infrastructure.kaltura.client

import com.boclips.cleanser.domain.model.MediaFilter
import com.boclips.cleanser.infrastructure.kaltura.KalturaProperties
import com.boclips.cleanser.infrastructure.kaltura.MediaItem
import mu.KLogging
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import java.net.URI


@Service
class KalturaMediaClient(
        val kalturaProperties: KalturaProperties,
        val retryHttpService: RetryHttpService) {
    companion object : KLogging()

    fun count(filters: List<MediaFilter> = emptyList()): Long {
        val body = buildRequestBody(pageSize = 1, pageIndex = 0, filters = filters)
        val request = HttpEntity<MultiValueMap<String, String>>(body, buildHeaders())
        return postWithRetries(request).count
    }

    fun fetch(pageSize: Int = 500, pageIndex: Int = 0, filters: List<MediaFilter> = emptyList()): List<MediaItem> {
        val request = HttpEntity<MultiValueMap<String, String>>(buildRequestBody(pageSize, pageIndex, filters), buildHeaders())
        return postWithRetries(request).items
    }

    private fun postWithRetries(request: HttpEntity<MultiValueMap<String, String>>): MediaList {
        try {
            return retryHttpService.post(request, URI("${kalturaProperties.host}/api_v3/service/media/action/list"))
        } catch (ex: Exception) {
            logger.error("Something went unexpectedly wrong ${request.body}, exception: $ex")
            throw KalturaClientException(ex)
        }
    }

    private fun buildRequestBody(pageSize: Int?, pageIndex: Int?, filters: List<MediaFilter>): LinkedMultiValueMap<String, String> {
        val params = LinkedMultiValueMap<String, String>()
        params.add("format", "1")
        params.add("ks", kalturaProperties.session)

        if (pageSize != null) params.add("pager[pageSize]", pageSize.toString())
        if (pageIndex != null) params.add("pager[pageIndex]", pageIndex.toString())

        filters.forEach { filter -> params.add(filter.key.filterKey, filter.value) }

        return params
    }

    private fun buildHeaders(): HttpHeaders {
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_FORM_URLENCODED
        headers.accept = mutableListOf(MediaType.ALL)
        return headers
    }
}

package com.boclips.cleanser.infrastructure.kaltura

import com.boclips.cleanser.infrastructure.kaltura.response.MediaItem
import com.boclips.cleanser.infrastructure.kaltura.response.MediaList
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import java.net.URI

@Component
class KalturaVideosRepository(@Value("kaltura.host") host: String) {
    fun getAllIds(): Set<String> {
        val mediaList: List<MediaItem> = fetchBatch()
        return mediaList.map { it.referenceId }.toSet()
    }

    private fun fetchBatch(): List<MediaItem> {
        val restTemplate = RestTemplate()
        // TODO: inject properties
        val response = restTemplate.exchange(URI("http://localhost:8089/api_v3/service/media/action/list"),
                HttpMethod.POST,
                null,
                MediaList::class.java)

        return response.body?.let { it.items }.orEmpty()
    }
}

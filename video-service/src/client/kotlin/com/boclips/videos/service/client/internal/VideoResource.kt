package com.boclips.videos.service.client.internal

import com.boclips.videos.service.client.VideoId
import com.boclips.videos.service.client.spring.Video
import java.net.URI

data class Link(val href: URI? = null)

data class Links(val self: Link? = null)

data class VideoResource(
        val _links: Links? = null,
        val subjects: Set<String>? = null,
        val contentPartner: String? = null

) {
    fun toVideo(): Video {
        return Video(
                videoId = VideoId(_links?.self?.href!!),
                subjects = subjects!!,
                contentPartnerId = contentPartner!!,
                contentPartnerVideoId = ""
        )
    }
}
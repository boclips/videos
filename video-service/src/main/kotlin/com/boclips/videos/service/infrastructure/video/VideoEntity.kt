package com.boclips.videos.service.infrastructure.video

import org.springframework.data.annotation.Id

class VideoEntity(
        @Id var id: Long,
        var source: String? = null,
        var namespace: String? = null,
        var title: String? = null,
        var description: String? = null,
        var date: String? = null,
        var duration: String? = null,
        var keywords: String? = null,
        var price_category: String? = null,
        var sounds: String? = null,
        var color: String? = null,
        var location: String? = null,
        var country: String? = null,
        var state: String? = null,
        var city: String? = null,
        var region: String? = null,
        var alternative_id: String? = null,
        var alt_source: String? = null,
        var restrictions: String? = null,
        var type_id: String? = null,
        var reference_id: String? = null
)

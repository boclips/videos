package com.boclips.videos.service.client.spring

import com.boclips.videos.service.client.VideoId

data class Video(
        val videoId: VideoId,
        val subjects: Set<String>,
        val contentPartnerId: String,
        val contentPartnerVideoId: String
)
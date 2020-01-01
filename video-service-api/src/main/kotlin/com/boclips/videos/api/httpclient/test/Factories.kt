package com.boclips.videos.api.httpclient.test

import com.boclips.videos.api.request.collection.CreateCollectionRequest
import com.boclips.videos.api.request.video.CreateVideoRequest
import java.time.LocalDate

class Factories {
    companion object {
        fun createCreateVideoRequest(
            providerVideoId: String? = "AP-1",
            providerId: String? = "provider-id",
            title: String? = "an AP video",
            description: String? = "an AP video about penguins",
            releasedOn: LocalDate? = LocalDate.now(),
            legalRestrictions: String? = "None",
            keywords: List<String>? = listOf("k1", "k2"),
            videoType: String? = "NEWS",
            playbackId: String? = "123",
            playbackProvider: String? = "KALTURA",
            analyseVideo: Boolean = false,
            subjects: Set<String> = setOf(),
            youtubeChannelId: String = "1234"
        ) = CreateVideoRequest(
            providerId = providerId,
            providerVideoId = providerVideoId,
            title = title,
            description = description,
            releasedOn = releasedOn,
            legalRestrictions = legalRestrictions,
            keywords = keywords,
            videoType = videoType,
            playbackId = playbackId,
            playbackProvider = playbackProvider,
            analyseVideo = analyseVideo,
            subjects = subjects,
            youtubeChannelId = youtubeChannelId
        )

        fun createCollectionRequest(
            title: String? = "collection title",
            description: String? = null,
            videos: List<String> = listOf(),
            public: Boolean? = null
        ) = CreateCollectionRequest(
            title = title,
            description = description,
            videos = videos,
            public = public
        )
    }
}
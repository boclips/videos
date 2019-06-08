package com.boclips.videos.service.client.testsupport

import com.boclips.videos.service.client.CreateVideoRequest
import com.boclips.videos.service.client.PlaybackProvider
import com.boclips.videos.service.client.VideoId
import com.boclips.videos.service.client.VideoType
import com.boclips.videos.service.testsupport.TestFactories
import java.net.URI
import java.time.LocalDate

object TestFactories {

    fun createVideoId(): VideoId {
        val id = TestFactories.aValidId()
        return VideoId(URI.create("https://video-service.com/v1/videos/$id"))
    }

    fun createCreateVideoRequest(
        contentProviderId: String = "ted",
        contentProviderVideoId: String = "ted-123",
        title: String = "video title",
        description: String = "video description",
        releasedOn: LocalDate = LocalDate.now(),
        legalRestrictions: String = "None",
        keywords: List<String> = listOf("k1", "k2"),
        contentType: VideoType = VideoType.INSTRUCTIONAL_CLIPS,
        playbackId: String = "kaltura-id-789",
        playbackProvider: PlaybackProvider = PlaybackProvider.KALTURA,
        subjects: Set<String> = emptySet()
    ): CreateVideoRequest {
        return CreateVideoRequest.builder()
            .provider(contentProviderId)
            .providerVideoId(contentProviderVideoId)
            .title(title)
            .description(description)
            .releasedOn(releasedOn)
            .legalRestrictions(legalRestrictions)
            .keywords(keywords)
            .videoType(contentType)
            .playbackId(playbackId)
            .playbackProvider(playbackProvider)
            .subjects(subjects)
            .build()
    }
}

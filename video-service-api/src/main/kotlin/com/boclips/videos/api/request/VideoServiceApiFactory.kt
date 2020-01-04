package com.boclips.videos.api.request

import com.boclips.videos.api.request.collection.CreateCollectionRequest
import com.boclips.videos.api.request.subject.CreateSubjectRequest
import com.boclips.videos.api.request.tag.CreateTagRequest
import com.boclips.videos.api.request.video.CreateVideoRequest
import com.boclips.videos.api.request.video.UpdateVideoRequest
import java.time.LocalDate

class VideoServiceApiFactory {
    companion object {
        fun createSubjectRequest(name: String? = null): CreateSubjectRequest = CreateSubjectRequest(name = name)

        fun createTagRequest(label: String? = null): CreateTagRequest = CreateTagRequest(label = label)

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

        fun createUpdateVideoRequest(
            title: String? = "video-title",
            description: String? = "description",
            promoted: Boolean? = false,
            subjectIds: List<String>? = emptyList()
        ): UpdateVideoRequest {
            return UpdateVideoRequest(
                title = title,
                description = description,
                promoted = promoted,
                subjectIds = subjectIds
            )
        }
    }
}

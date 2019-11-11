package com.boclips.videos.service.presentation.video

import com.boclips.videos.service.application.exceptions.NonNullableFieldCreateRequestException.Companion.getOrThrow
import com.boclips.videos.service.domain.model.common.AgeRange
import com.boclips.videos.service.domain.model.playback.VideoPlayback
import com.boclips.videos.service.domain.model.subject.Subject
import com.boclips.videos.service.domain.model.video.ContentPartner
import com.boclips.videos.service.domain.model.video.DistributionMethod
import com.boclips.videos.service.domain.model.video.ContentType
import com.boclips.videos.service.domain.model.video.Video
import com.boclips.videos.service.domain.model.video.VideoId
import org.bson.types.ObjectId

// TODO refactor to use javax validation in the first place
class CreateVideoRequestToVideoConverter {
    fun convert(
        createVideoRequest: CreateVideoRequest,
        videoPlayback: VideoPlayback,
        contentPartner: ContentPartner,
        distributionMethods: Set<DistributionMethod>,
        subjects: List<Subject>
    ): Video {
        return Video(
            videoId = VideoId(value = ObjectId().toHexString()),
            playback = videoPlayback,
            title = getOrThrow(createVideoRequest.title, "title"),
            description = getOrThrow(createVideoRequest.description, "description"),
            keywords = getOrThrow(createVideoRequest.keywords, "keywords"),
            releasedOn = getOrThrow(createVideoRequest.releasedOn, "releasedOn"),
            contentPartner = contentPartner,
            videoReference = getOrThrow(createVideoRequest.providerVideoId, "providerVideoId"),
            type = ContentType.valueOf(getOrThrow(createVideoRequest.videoType, "videoType")),
            legalRestrictions = createVideoRequest.legalRestrictions ?: "",
            ageRange = if (createVideoRequest.ageRangeMin !== null) {
                AgeRange.bounded(createVideoRequest.ageRangeMin, createVideoRequest.ageRangeMax)
            } else {
                AgeRange.unbounded()
            },
            subjects = subjects.toSet(),
            topics = emptySet(),
            language = null,
            transcript = null,
            distributionMethods = distributionMethods,
            ratings = emptyList(),
            tag = null,
            promoted = null
        )
    }
}

package com.boclips.videos.service.presentation.video

import com.boclips.videos.service.application.exceptions.NonNullableFieldCreateRequestException.Companion.getOrThrow
import com.boclips.videos.service.domain.model.Video
import com.boclips.videos.service.domain.model.ageRange.AgeRange
import com.boclips.videos.service.domain.model.contentPartner.ContentPartner
import com.boclips.videos.service.domain.model.playback.VideoPlayback
import com.boclips.videos.service.domain.model.video.LegacySubject
import com.boclips.videos.service.domain.model.video.LegacyVideoType
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.domain.model.video.VideoOwner
import org.bson.types.ObjectId

// TODO refactor to use javax validation in the first place
class CreateVideoRequestToVideoConverter {
    fun convert(
        createVideoRequest: CreateVideoRequest,
        videoPlayback: VideoPlayback,
        contentPartner: ContentPartner
    ): Video {
        return Video(
            videoId = VideoId(value = ObjectId().toHexString()),
            playback = videoPlayback,
            title = getOrThrow(createVideoRequest.title, "title"),
            description = getOrThrow(createVideoRequest.description, "description"),
            keywords = getOrThrow(createVideoRequest.keywords, "keywords"),
            releasedOn = getOrThrow(createVideoRequest.releasedOn, "releasedOn"),
            owner = VideoOwner(
                contentPartnerId = contentPartner.contentPartnerId,
                name = contentPartner.name,
                videoReference = getOrThrow(createVideoRequest.providerVideoId, "providerVideoId")
            ),
            type = LegacyVideoType.valueOf(getOrThrow(createVideoRequest.videoType, "videoType")),
            legalRestrictions = createVideoRequest.legalRestrictions ?: "",
            subjects = getOrThrow(createVideoRequest.subjects, "subjects").map { LegacySubject(it) }.toSet(),
            language = null,
            transcript = null,
            topics = emptySet(),
            searchable = createVideoRequest.searchable ?: true,
            ageRange = if (createVideoRequest.ageRangeMin !== null) {
                AgeRange.bounded(createVideoRequest.ageRangeMin, createVideoRequest.ageRangeMax)
            } else {
                AgeRange.unbounded()
            }
        )
    }
}
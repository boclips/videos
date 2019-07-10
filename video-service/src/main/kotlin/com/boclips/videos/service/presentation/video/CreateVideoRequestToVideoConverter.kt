package com.boclips.videos.service.presentation.video

import com.boclips.videos.service.application.exceptions.NonNullableFieldCreateRequestException.Companion.getOrThrow
import com.boclips.videos.service.domain.model.Video
import com.boclips.videos.service.domain.model.common.AgeRange
import com.boclips.videos.service.domain.model.contentPartner.ContentPartner
import com.boclips.videos.service.domain.model.playback.VideoPlayback
import com.boclips.videos.service.domain.model.subjects.Subject
import com.boclips.videos.service.domain.model.video.DeliveryMethod
import com.boclips.videos.service.domain.model.video.LegacyVideoType
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.presentation.deliveryMethod.DeliveryMethodResourceConverter
import org.bson.types.ObjectId

// TODO refactor to use javax validation in the first place
class CreateVideoRequestToVideoConverter {
    fun convert(
        createVideoRequest: CreateVideoRequest,
        videoPlayback: VideoPlayback,
        contentPartner: ContentPartner,
        subjects: List<Subject>
    ): Video {
        val searchable = if (!contentPartner.searchable) {
            contentPartner.searchable
        } else {
            createVideoRequest.searchable ?: true
        }
        return Video(
            videoId = VideoId(value = ObjectId().toHexString()),
            playback = videoPlayback,
            title = getOrThrow(createVideoRequest.title, "title"),
            description = getOrThrow(createVideoRequest.description, "description"),
            keywords = getOrThrow(createVideoRequest.keywords, "keywords"),
            releasedOn = getOrThrow(createVideoRequest.releasedOn, "releasedOn"),
            contentPartner = contentPartner,
            videoReference = getOrThrow(createVideoRequest.providerVideoId, "providerVideoId"),
            type = LegacyVideoType.valueOf(getOrThrow(createVideoRequest.videoType, "videoType")),
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
            rating = null,
            hiddenFromSearchForDeliveryMethods = createVideoRequest.hiddenFromSearchForDeliveryMethods
                ?.map(DeliveryMethodResourceConverter::fromResource)
                ?.toSet()
                ?: deliveryMethodsFromLegacySearchable(searchable)
        )
    }

    private fun deliveryMethodsFromLegacySearchable(searchable: Boolean): Set<DeliveryMethod> =
        if (searchable) {
            emptySet()
        } else {
            DeliveryMethod.ALL
        }
}

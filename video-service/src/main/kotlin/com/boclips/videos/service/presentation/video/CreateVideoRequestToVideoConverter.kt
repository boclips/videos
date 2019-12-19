package com.boclips.videos.service.presentation.video

import com.boclips.videos.service.domain.model.AgeRange
import com.boclips.videos.service.domain.model.playback.VideoPlayback
import com.boclips.videos.service.domain.model.subject.Subject
import com.boclips.videos.service.domain.model.video.ContentPartner
import com.boclips.videos.service.domain.model.video.ContentType
import com.boclips.videos.service.domain.model.video.Video
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.domain.model.video.VideoSubjects
import org.bson.types.ObjectId
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.ZonedDateTime

class CreateVideoRequestToVideoConverter {
    fun convert(
        createVideoRequest: CreateVideoRequest,
        videoPlayback: VideoPlayback,
        contentPartner: ContentPartner,
        subjects: List<Subject>
    ): Video {
        return Video(
            videoId = VideoId(value = ObjectId().toHexString()),
            playback = videoPlayback,
            title =createVideoRequest.title!!,
            description = createVideoRequest.description!!,
            keywords = createVideoRequest.keywords!!,
            releasedOn = createVideoRequest.releasedOn!!,
            ingestedOn = LocalDate.now(),
            ingestedAt = ZonedDateTime.now(ZoneOffset.UTC),
            contentPartner = contentPartner,
            videoReference = createVideoRequest.providerVideoId!!,
            type = ContentType.valueOf(createVideoRequest.videoType!!),
            legalRestrictions = createVideoRequest.legalRestrictions ?: "",
            ageRange = if (createVideoRequest.ageRangeMin !== null) {
                AgeRange.bounded(createVideoRequest.ageRangeMin, createVideoRequest.ageRangeMax)
            } else {
                AgeRange.unbounded()
            },
            subjects =
                VideoSubjects(
                    setManually = subjects.isNotEmpty(),
                    items = subjects.toSet()
                ),
            topics = emptySet(),
            language = null,
            transcript = null,
            ratings = emptyList(),
            tag = null,
            promoted = null,
            shareCodes = emptySet()
        )
    }
}

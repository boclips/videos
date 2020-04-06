package com.boclips.videos.service.presentation.converters

import com.boclips.videos.api.request.video.CreateVideoRequest
import com.boclips.videos.service.domain.model.AgeRange
import com.boclips.videos.service.domain.model.playback.VideoPlayback
import com.boclips.videos.service.domain.model.subject.Subject
import com.boclips.videos.service.domain.model.video.ContentPartner
import com.boclips.videos.service.domain.model.video.ContentType
import com.boclips.videos.service.domain.model.video.Video
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.domain.model.video.VideoSubjects
import org.bson.types.ObjectId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.Locale

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
            title = createVideoRequest.title!!,
            description = createVideoRequest.description!!,
            keywords = createVideoRequest.keywords!!,
            releasedOn = createVideoRequest.releasedOn!!,
            ingestedAt = ZonedDateTime.now(ZoneOffset.UTC),
            contentPartner = contentPartner,
            videoReference = createVideoRequest.providerVideoId!!,
            type = ContentType.valueOf(createVideoRequest.videoType!!),
            legalRestrictions = createVideoRequest.legalRestrictions ?: "",
            ageRange = AgeRange.of(
                min = createVideoRequest.ageRangeMin,
                max = createVideoRequest.ageRangeMax,
                curatedManually = false
            ),
            subjects = VideoSubjects(
                setManually = subjects.isNotEmpty(),
                items = subjects.toSet()
            ),
            topics = emptySet(),
            language = createVideoRequest.language?.let { Locale.forLanguageTag(it) },
            transcript = null,
            ratings = emptyList(),
            tags = emptyList(),
            promoted = null,
            attachments = emptyList()
        )
    }
}

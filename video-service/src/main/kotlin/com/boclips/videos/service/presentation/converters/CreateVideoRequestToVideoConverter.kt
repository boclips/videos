package com.boclips.videos.service.presentation.converters

import com.boclips.videos.api.request.video.CreateVideoRequest
import com.boclips.videos.service.domain.model.AgeRange
import com.boclips.videos.service.domain.model.playback.VideoPlayback
import com.boclips.videos.service.domain.model.subject.Subject
import com.boclips.videos.service.domain.model.video.ContentType
import com.boclips.videos.service.domain.model.video.Video
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.domain.model.video.VideoSubjects
import com.boclips.videos.service.domain.model.video.Voice
import com.boclips.videos.service.domain.model.video.channel.Channel
import org.bson.types.ObjectId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.Locale

class CreateVideoRequestToVideoConverter {
    fun convert(
        createVideoRequest: CreateVideoRequest,
        videoPlayback: VideoPlayback,
        contentPartner: Channel,
        subjects: List<Subject>
    ): Video {
        return Video(
            videoId = VideoId(value = ObjectId().toHexString()),
            playback = videoPlayback,
            title = createVideoRequest.title!!,
            description = createVideoRequest.description!!,
            additionalDescription = createVideoRequest.additionalDescription,
            keywords = createVideoRequest.keywords!!,
            releasedOn = createVideoRequest.releasedOn!!,
            ingestedAt = ZonedDateTime.now(ZoneOffset.UTC),
            channel = contentPartner,
            videoReference = createVideoRequest.providerVideoId!!,
            types = createVideoRequest.videoTypes!!.map { ContentType.valueOf(it) },
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
            voice = Voice.UnknownVoice(
                language = createVideoRequest.language?.let { Locale.forLanguageTag(it) },
                transcript = null
            ),
            ratings = emptyList(),
            tags = emptyList(),
            promoted = null,
            attachments = emptyList(),
            contentWarnings = emptyList(),
            deactivated = false,
            activeVideoId = null
        )
    }
}

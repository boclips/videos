package com.boclips.videos.service.presentation.converters

import com.boclips.videos.api.response.agerange.AgeRangeResource
import com.boclips.videos.api.response.subject.SubjectResource
import com.boclips.videos.api.response.video.TagResource
import com.boclips.videos.api.response.video.VideoBadge
import com.boclips.videos.api.response.video.VideoResource
import com.boclips.videos.api.response.video.VideoTypeResource
import com.boclips.videos.service.domain.model.User
import com.boclips.videos.service.domain.model.playback.VideoPlayback.YoutubePlayback
import com.boclips.videos.service.domain.model.video.Video
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.presentation.hateoas.VideosLinkBuilder
import org.springframework.stereotype.Component

@Component
class VideoToResourceConverter(
    private val videosLinkBuilder: VideosLinkBuilder,
    private val playbackToResourceConverter: PlaybackToResourceConverter
) {
    fun convertVideos(videos: List<Video>, user: User): List<VideoResource> {
        return videos.map { video -> convertVideo(video, user) }
    }

    fun convertVideo(video: Video, user: User): VideoResource {
        return VideoResource(
            id = video.videoId.value,
            title = video.title,
            description = video.description,
            createdBy = video.contentPartner.name,
            contentPartner = video.contentPartner.name,
            contentPartnerId = video.contentPartner.contentPartnerId.value,
            contentPartnerVideoId = video.videoReference,
            releasedOn = video.releasedOn,
            playback = playbackToResourceConverter.convert(video.playback),
            subjects = video.subjects.items.map { SubjectResource(id = it.id.value, name = it.name) }.toSet(),
            badges = getBadges(video),
            type = VideoTypeResource(id = video.type.id, name = video.type.title),
            legalRestrictions = video.legalRestrictions,
            hasTranscripts = video.transcript != null,
            ageRange = getAgeRange(video),
            rating = video.getRatingAverage(),
            yourRating = video.ratings.firstOrNull { it.userId == user.id }?.rating?.toDouble(),
            bestFor = video.tag?.let { listOf(TagResource(it.tag.label)) } ?: emptyList(),
            bestForTags = video.tag?.let { listOf(TagResource(it.tag.label)) } ?: emptyList(),
            promoted = video.promoted,
            _links = (resourceLinks(video.videoId.value) + actionLinks(video)).map { it.rel to it }.toMap()
        )
    }

    fun convertVideoIds(videoIds: List<VideoId>): List<VideoResource> {
        return videoIds.map { videoId ->
            VideoResource(
                id = videoId.value,
                _links = resourceLinks(videoId.value).map { it.rel to it }.toMap()
            )
        }
    }

    private fun getAgeRange(video: Video): AgeRangeResource? {
        return AgeRangeToResourceConverter.convert(video.ageRange)
    }

    private fun getBadges(video: Video): Set<String> {
        return when (video.playback) {
            is YoutubePlayback -> setOf(VideoBadge.YOUTUBE.id)
            else -> setOf(VideoBadge.AD_FREE.id)
        }
    }

    private fun resourceLinks(videoId: String) =
        listOfNotNull(
            videosLinkBuilder.self(videoId),
            videosLinkBuilder.createVideoInteractedWithEvent(videoId)
        )

    private fun actionLinks(video: Video) = listOfNotNull(
        videosLinkBuilder.rateLink(video),
        videosLinkBuilder.updateLink(video),
        videosLinkBuilder.tagLink(video),
        videosLinkBuilder.shareLink(video),
        videosLinkBuilder.validateShareCodeLink(video),
        videosLinkBuilder.transcriptLink(video)
    )
}

package com.boclips.videos.service.presentation.converters

import com.boclips.videos.api.request.video.PlaybackResource
import com.boclips.videos.api.response.video.TagResource
import com.boclips.videos.api.response.video.VideoBadge
import com.boclips.videos.api.response.video.VideoResource
import com.boclips.videos.api.response.video.VideoTypeResource
import com.boclips.videos.service.domain.model.User
import com.boclips.videos.service.domain.model.playback.VideoPlayback.YoutubePlayback
import com.boclips.videos.service.domain.model.video.Video
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.api.response.agerange.AgeRangeResource
import com.boclips.videos.service.presentation.hateoas.VideosLinkBuilder
import com.boclips.videos.api.response.subject.SubjectResource
import org.springframework.hateoas.Resource
import org.springframework.stereotype.Component

@Component
class VideoToResourceConverter(
    private val videosLinkBuilder: VideosLinkBuilder,
    private val playbackToResourceConverter: PlaybackToResourceConverter
) {
    fun wrapVideosInResource(videos: List<Video>, user: User): List<Resource<VideoResource>> {
        return videos.map { video -> fromVideo(video, user) }
    }

    fun wrapVideoIdsInResource(videoIds: List<VideoId>): List<Resource<VideoResource>> {
        return videoIds.map { videoId -> wrapResourceWithHateoas(VideoResource(id = videoId.value), null) }
    }

    fun fromVideo(video: Video, user: User): Resource<VideoResource> {
        return toResource(video, user)
    }

    private fun toResource(video: Video, user: User): Resource<VideoResource> {
        return wrapResourceWithHateoas(
            VideoResource(
                id = video.videoId.value,
                title = video.title,
                description = video.description,
                createdBy = video.contentPartner.name,
                contentPartner = video.contentPartner.name,
                contentPartnerId = video.contentPartner.contentPartnerId.value,
                contentPartnerVideoId = video.videoReference,
                releasedOn = video.releasedOn,
                playback = getPlayback(video),
                subjects = video.subjects.items.map { SubjectResource(id = it.id.value, name = it.name) }.toSet(),
                badges = getBadges(video),
                type = VideoTypeResource(id = video.type.id, name = video.type.title),
                legalRestrictions = video.legalRestrictions,
                hasTranscripts = video.transcript != null,
                ageRange = getAgeRange(video),
                rating = video.getRatingAverage(),
                yourRating = video.ratings.firstOrNull { it.userId == user.id }?.rating?.toDouble(),
                bestFor = video.tag?.let { TagResource(it.tag.label) },
                promoted = video.promoted
            ), video
        )
    }

    private fun getAgeRange(video: Video): AgeRangeResource? {
        return AgeRangeToResourceConverter.convert(video.ageRange)
    }

    private fun getPlayback(video: Video): Resource<PlaybackResource> {
        return playbackToResourceConverter.wrapPlaybackInResource(video.playback)
    }

    private fun getBadges(video: Video): Set<String> {
        return when (video.playback) {
            is YoutubePlayback -> setOf(VideoBadge.YOUTUBE.id)
            else -> setOf(VideoBadge.AD_FREE.id)
        }
    }

    private fun wrapResourceWithHateoas(
        videoResource: VideoResource,
        video: Video?
    ) = Resource(
        videoResource,
        listOfNotNull(
            videosLinkBuilder.self(videoResource),
            videosLinkBuilder.transcriptLink(videoResource),
            videosLinkBuilder.createVideoInteractedWithEvent(videoResource),
            video?.let { videosLinkBuilder.rateLink(it) },
            video?.let { videosLinkBuilder.updateLink(it) },
            video?.let { videosLinkBuilder.tagLink(it) },
            video?.let { videosLinkBuilder.shareLink(it) },
            video?.let { videosLinkBuilder.validateShareCodeLink(it) }
        )
    )
}

package com.boclips.videos.service.presentation.converters

import com.boclips.videos.api.response.agerange.AgeRangeResource
import com.boclips.videos.api.response.subject.SubjectResource
import com.boclips.videos.api.response.video.LanguageResource
import com.boclips.videos.api.response.video.TagResource
import com.boclips.videos.api.response.video.VideoBadge
import com.boclips.videos.api.response.video.VideoResource
import com.boclips.videos.api.response.video.VideoTypeResource
import com.boclips.videos.api.response.video.VideosResource
import com.boclips.videos.api.response.video.VideosWrapperResource
import com.boclips.videos.service.common.Page
import com.boclips.videos.service.domain.model.User
import com.boclips.videos.service.domain.model.playback.VideoPlayback.YoutubePlayback
import com.boclips.videos.service.domain.model.video.Video
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.presentation.hateoas.VideosLinkBuilder
import org.springframework.hateoas.PagedModel

class VideoToResourceConverter(
    private val videosLinkBuilder: VideosLinkBuilder,
    private val playbackToResourceConverter: PlaybackToResourceConverter
) {
    fun convert(videos: List<Video>, user: User): List<VideoResource> {
        return videos.map { video -> convert(video, user) }
    }

    fun convert(videos: Page<Video>, user: User): VideosResource {
        return VideosResource(
            _embedded = VideosWrapperResource(videos = videos
                .elements.toList()
                .map { video -> convert(video, user) }),
            page = PagedModel.PageMetadata(
                videos.pageInfo.pageRequest.size.toLong(),
                videos.pageInfo.pageRequest.page.toLong(),
                videos.pageInfo.totalElements
            ),
            _links = null
        )
    }

    fun convert(video: Video, user: User): VideoResource {
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
            bestFor = video.tags.map { TagResource(it.tag.label) },
            promoted = video.promoted,
            language = video.language?.let { LanguageResource.from(it) },
            _links = (resourceLinks(video.videoId.value) + actionLinks(video)).map { it.rel.value() to it }.toMap()
        )
    }

    fun convertVideoIds(videoIds: List<VideoId>): List<VideoResource> {
        return videoIds.map { videoId ->
            VideoResource(
                id = videoId.value,
                _links = resourceLinks(videoId.value).map { it.rel.value() to it }.toMap()
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
        videosLinkBuilder.transcriptLink(video)
    )
}

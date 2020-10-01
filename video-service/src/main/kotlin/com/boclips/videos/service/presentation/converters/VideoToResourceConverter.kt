package com.boclips.videos.service.presentation.converters

import com.boclips.contentpartner.service.application.channel.GetChannels
import com.boclips.videos.api.response.HateoasLink
import com.boclips.videos.api.response.agerange.AgeRangeResource
import com.boclips.videos.api.response.subject.SubjectResource
import com.boclips.videos.api.response.video.LanguageResource
import com.boclips.videos.api.response.video.TagResource
import com.boclips.videos.api.response.video.VideoBadge
import com.boclips.videos.api.response.video.VideoFacetResource
import com.boclips.videos.api.response.video.VideoFacetsResource
import com.boclips.videos.api.response.video.VideoResource
import com.boclips.videos.api.response.video.VideoTypeResource
import com.boclips.videos.api.response.video.VideosResource
import com.boclips.videos.api.response.video.VideosWrapperResource
import com.boclips.videos.service.common.ResultsPage
import com.boclips.videos.service.domain.model.playback.VideoPlayback.YoutubePlayback
import com.boclips.videos.service.domain.model.user.User
import com.boclips.videos.service.domain.model.video.ChannelFacet
import com.boclips.videos.service.domain.model.video.Video
import com.boclips.videos.service.domain.model.video.VideoCounts
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.presentation.hateoas.VideosLinkBuilder
import org.springframework.hateoas.PagedModel

class VideoToResourceConverter(
    private val videosLinkBuilder: VideosLinkBuilder,
    private val playbackToResourceConverter: PlaybackToResourceConverter,
    private val attachmentToResourceConverter: AttachmentToResourceConverter,
    private val contentWarningToResourceConverter: ContentWarningToResourceConverter,
    private val getChannels: GetChannels
) {
    fun convert(videos: List<Video>, user: User): List<VideoResource> {
        return videos.map { video -> convert(video, user) }
    }

    fun convert(resultsPage: ResultsPage<Video, VideoCounts>, user: User): VideosResource {
        return VideosResource(
            _embedded = VideosWrapperResource(
                videos = resultsPage
                    .elements.toList()
                    .map { video -> convert(video, user) },
                facets = convertFacets(resultsPage)
            ),
            page = PagedModel.PageMetadata(
                resultsPage.pageInfo.pageRequest.size.toLong(),
                resultsPage.pageInfo.pageRequest.page.toLong(),
                resultsPage.pageInfo.totalElements
            ),
            _links = null
        )
    }

    fun convert(video: Video, user: User, omitProtectedAttributes: Boolean? = false): VideoResource {
        return VideoResource(
            id = video.videoId.value,
            title = video.title,
            description = video.description,
            additionalDescription = video.additionalDescription,
            createdBy = video.channel.name,
            channel = video.channel.name,
            channelId = video.channel.channelId.value,
            channelVideoId = video.videoReference,
            releasedOn = video.releasedOn,
            playback = playbackToResourceConverter.convert(video.playback, video.videoId, omitProtectedAttributes),
            subjects = video.subjects.items.map { SubjectResource(id = it.id.value, name = it.name) }.toSet(),
            badges = convertBadges(video),
            types = video.types.map { VideoTypeResource(id = it.id, name = it.title) },
            legalRestrictions = video.legalRestrictions,
            hasTranscripts = video.voice.transcript != null,
            ageRange = convertAgeRange(video),
            rating = video.getRatingAverage(),
            yourRating = video.ratings.firstOrNull { it.userId == user.id }?.rating?.toDouble(),
            bestFor = video.tags.map { TagResource(it.tag.label) },
            promoted = video.promoted,
            language = video.voice.language?.let { LanguageResource.from(it) },
            isVoiced = video.isVoiced(),
            attachments = when (omitProtectedAttributes) {
                true -> emptyList()
                else -> video.attachments.map { attachmentToResourceConverter.convert(it) }
            },
            contentWarnings = video.contentWarnings?.map { contentWarningToResourceConverter.convert(it) },
            _links = (
                resourceLinks(video.videoId.value) +
                    conditionalResourceLinks(video) +
                    actionLinks(video)
                )
                .map { it.rel to it }
                .toMap()
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

    private fun convertFacets(resultsPage: ResultsPage<Video, VideoCounts>): VideoFacetsResource? {
        return resultsPage.counts?.let { counts ->
            VideoFacetsResource(
                subjects = counts.subjects.map {
                    it.subjectId.value to VideoFacetResource(
                        hits = it.total
                    )
                }.toMap(),
                ageRanges = counts.ageRanges.map {
                    it.ageRangeId.value to VideoFacetResource(
                        hits = it.total
                    )
                }.toMap(),
                durations = counts.durations.map {
                    it.durationId to VideoFacetResource(hits = it.total)
                }.toMap(),
                resourceTypes = counts.attachmentTypes.map {
                    it.attachmentType to VideoFacetResource(hits = it.total)
                }.toMap(),
                channels = toChannelFacetResource(counts.channels)
            )
        }
    }

    private fun convertAgeRange(video: Video): AgeRangeResource? {
        return AgeRangeToResourceConverter.convert(video.ageRange)
    }

    private fun convertBadges(video: Video): Set<String> {
        return when (video.playback) {
            is YoutubePlayback -> setOf(VideoBadge.YOUTUBE.id)
            else -> setOf(VideoBadge.AD_FREE.id)
        }
    }

    private fun toChannelFacetResource(channelFacets: List<ChannelFacet>):  Map<String, VideoFacetResource> {
        val channels = getChannels()
        return channelFacets.mapNotNull { channelFacet ->
            channels
                .find { channel -> channel.id.value == channelFacet.channelId.value }
                ?.let { it.name to VideoFacetResource(hits = channelFacet.total, id = it.id.value) }
        }.toMap()
    }

    private fun resourceLinks(videoId: String) =
        listOfNotNull(
            videosLinkBuilder.self(videoId),
            videosLinkBuilder.createVideoInteractedWithEvent(videoId),
            videosLinkBuilder.videoDetailsProjection(videoId),
            videosLinkBuilder.videoFullProjection(videoId)
        )

    private fun conditionalResourceLinks(video: Video) = listOfNotNull(
        videosLinkBuilder.assets(video)
    )

    private fun actionLinks(video: Video): List<HateoasLink> = listOfNotNull(
        videosLinkBuilder.rateLink(video),
        videosLinkBuilder.updateLink(video),
        videosLinkBuilder.addAttachment(video),
        videosLinkBuilder.tagLink(video),
        videosLinkBuilder.transcriptLink(video)
    )
}

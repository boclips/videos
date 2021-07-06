package com.boclips.videos.service.presentation.converters

import com.boclips.videos.api.response.HateoasLink
import com.boclips.videos.api.response.agerange.AgeRangeResource
import com.boclips.videos.api.response.channel.TaxonomyCategoryResource
import com.boclips.videos.api.response.subject.SubjectResource
import com.boclips.videos.api.response.video.*
import com.boclips.videos.service.application.channels.VideoChannelService
import com.boclips.videos.service.application.subject.GetSubjects
import com.boclips.videos.service.common.ResultsPage
import com.boclips.videos.service.domain.model.playback.VideoPlayback.YoutubePlayback
import com.boclips.videos.service.domain.model.taxonomy.CategorySource
import com.boclips.videos.service.domain.model.taxonomy.CategoryWithAncestors
import com.boclips.videos.service.domain.model.user.User
import com.boclips.videos.service.domain.model.video.*
import com.boclips.videos.service.domain.model.video.prices.PricedVideo
import com.boclips.videos.service.domain.service.taxonomy.CategoryService
import com.boclips.videos.service.presentation.hateoas.VideosLinkBuilder
import org.springframework.hateoas.PagedModel

class VideoToResourceConverter(
    private val videosLinkBuilder: VideosLinkBuilder,
    private val playbackToResourceConverter: PlaybackToResourceConverter,
    private val attachmentToResourceConverter: AttachmentToResourceConverter,
    private val contentWarningToResourceConverter: ContentWarningToResourceConverter,
    private val videoChannelService: VideoChannelService,
    private val getSubjects: GetSubjects,
    private val categoryResourceConverter: CategoryResourceConverter,
    private val categoryService: CategoryService

) {
    fun convert(videos: List<Video>, user: User): List<VideoResource> {
        return videos.map { video -> convert(video, user) }
    }

    fun convert(resultsPage: ResultsPage<out BaseVideo, VideoCounts>, user: User): VideosResource {
        return VideosResource(
            _embedded = VideosWrapperResource(
                videos = resultsPage
                    .elements.toList()
                    .map { video -> convert(video, user) },
                facets = convertFacets(resultsPage.counts)
            ),
            page = PagedModel.PageMetadata(
                resultsPage.pageInfo.pageRequest.size.toLong(),
                resultsPage.pageInfo.pageRequest.page.toLong(),
                resultsPage.pageInfo.totalElements
            ),
            _links = null
        )
    }

    fun convert(video: BaseVideo, user: User, omitProtectedAttributes: Boolean? = false): VideoResource {
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
            updatedAt = video.updatedAt,
            playback = playbackToResourceConverter.convert(video.playback, video.videoId, omitProtectedAttributes),
            subjects = video.subjects.items.map { SubjectResource(id = it.id.value, name = it.name) }.toSet(),
            badges = convertBadges(video),
            types = video.types.map { VideoTypeResource(id = it.id, name = resolveVideoTypeName(it)) },
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
            price = if (video is PricedVideo) video.price?.toResource() else null,
            contentWarnings = video.contentWarnings?.map { contentWarningToResourceConverter.convert(it) },
            keywords = video.keywords,
            categories = createCategories(video.categories),
            taxonomy = VideoTaxonomyResourceWrapper(
                channel = VideoTaxonomyResource(
                    categories = video.categories[CategorySource.CHANNEL]?.map {
                        TaxonomyCategoryResource(
                            codeValue = it.codeValue.value,
                            description = it.description,
                            ancestors = it.ancestors.map { ancestor -> ancestor.value }.toSet()
                        )
                    }
                ),
                manual = VideoTaxonomyResource(
                    categories = video.categories[CategorySource.MANUAL]?.map {
                        TaxonomyCategoryResource(
                            codeValue = it.codeValue.value,
                            description = it.description,
                            ancestors = it.ancestors.map { ancestor -> ancestor.value }.toSet()
                        )
                    }
                )
            ),
            _links = (
                resourceLinks(video.videoId.value) +
                    conditionalResourceLinks(video) +
                    actionLinks(video)
                )
                .map { it.rel to it }
                .toMap()
        )
    }

    private fun createCategories(categories: Map<CategorySource, Set<CategoryWithAncestors>>): List<VideoCategoryResource>? {
        return categories.flatMap { categoryTypes ->
            categoryTypes.value.map { category ->
                categoryService.buildTreeFromChild(category).let {
                    categoryResourceConverter.convertTree(it)
                }
            }
        }
    }

    fun convertVideoIds(videoIds: List<VideoId>): List<VideoResource> {
        return videoIds.map { videoId ->
            VideoResource(
                id = videoId.value,
                _links = resourceLinks(videoId.value).associateBy { it.rel }
            )
        }
    }

    private fun Price.toResource() = PriceResource(amount = amount, currency = currency)

    private fun convertFacets(counts: VideoCounts?): VideoFacetsResource? {
        return counts?.let {
            VideoFacetsResource(
                subjects = toSubjectFacetResource(counts.subjects),
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
                channels = toChannelFacetResource(counts.channels),
                videoTypes = counts.videoTypes.map {
                    it.typeId.toUpperCase() to VideoFacetResource(hits = it.total)
                }.toMap(),
                prices = counts.prices.map {
                    it.price to VideoFacetResource(name = it.price, hits = it.total)
                }.toMap()
            )
        }
    }

    private fun toSubjectFacetResource(subjectFacets: List<SubjectFacet>): Map<String, VideoFacetResource> {
        val subjects = getSubjects().associateBy({ it.id }, { it })

        return subjectFacets.map {
            it.subjectId.value to VideoFacetResource(
                id = it.subjectId.value,
                name = subjects[it.subjectId.value]?.name,
                hits = it.total
            )
        }.toMap()
    }

    private fun convertAgeRange(video: BaseVideo): AgeRangeResource? {
        return AgeRangeToResourceConverter.convert(video.ageRange)
    }

    private fun convertBadges(video: BaseVideo): Set<String> {
        return when (video.playback) {
            is YoutubePlayback -> setOf(VideoBadge.YOUTUBE.id)
            else -> setOf(VideoBadge.AD_FREE.id)
        }
    }

    private fun toChannelFacetResource(channelFacets: List<ChannelFacet>): Map<String, VideoFacetResource> {
        val channels = videoChannelService.findAllByIds(channelFacets.map { it.channelId })
        return channelFacets.mapNotNull { channelFacet ->
            channels
                .find { channel -> channel.channelId.value == channelFacet.channelId.value }
                ?.let {
                    it.channelId.value to VideoFacetResource(
                        id = it.channelId.value,
                        name = it.name,
                        hits = channelFacet.total
                    )
                }
        }.toMap()
    }

    private fun resourceLinks(videoId: String) =
        listOfNotNull(
            videosLinkBuilder.self(videoId),
            videosLinkBuilder.createVideoInteractedWithEvent(videoId),
            videosLinkBuilder.videoDetailsProjection(videoId),
            videosLinkBuilder.videoFullProjection(videoId)
        )

    private fun conditionalResourceLinks(video: BaseVideo) = listOfNotNull(
        videosLinkBuilder.assets(video)
    )

    private fun actionLinks(video: BaseVideo): List<HateoasLink> = listOfNotNull(
        videosLinkBuilder.rateLink(video),
        videosLinkBuilder.updateLink(video),
        videosLinkBuilder.addAttachment(video),
        videosLinkBuilder.tagLink(video),
        videosLinkBuilder.transcriptLink(video)
    )

    private fun resolveVideoTypeName(videoType: VideoType) = when (videoType) {
        VideoType.NEWS -> "News"
        VideoType.STOCK -> "Stock"
        VideoType.INSTRUCTIONAL_CLIPS -> "Instructional Clips"
    }
}

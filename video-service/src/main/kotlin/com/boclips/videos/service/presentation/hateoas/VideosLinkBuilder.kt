package com.boclips.videos.service.presentation.hateoas

import com.boclips.security.utils.UserExtractor.currentUserHasRole
import com.boclips.security.utils.UserExtractor.getIfAuthenticated
import com.boclips.security.utils.UserExtractor.getIfHasAnyRole
import com.boclips.security.utils.UserExtractor.getIfHasRole
import com.boclips.videos.api.request.Projection
import com.boclips.videos.api.response.HateoasLink
import com.boclips.videos.service.config.security.UserRoles
import com.boclips.videos.service.domain.model.video.BaseVideo
import com.boclips.videos.service.domain.model.video.Video
import com.boclips.videos.service.presentation.VideoController
import com.boclips.videos.service.presentation.hateoas.VideosLinkBuilder.Rels.ADD_ATTACHMENT
import com.boclips.videos.service.presentation.hateoas.VideosLinkBuilder.Rels.GET_CAPTIONS
import com.boclips.videos.service.presentation.hateoas.VideosLinkBuilder.Rels.GET_METADATA
import com.boclips.videos.service.presentation.hateoas.VideosLinkBuilder.Rels.LOG_VIDEO_INTERACTION
import com.boclips.videos.service.presentation.hateoas.VideosLinkBuilder.Rels.SEARCH_VIDEOS
import com.boclips.videos.service.presentation.hateoas.VideosLinkBuilder.Rels.UPDATE
import com.boclips.videos.service.presentation.hateoas.VideosLinkBuilder.Rels.UPDATE_CAPTIONS
import com.boclips.videos.service.presentation.hateoas.VideosLinkBuilder.Rels.VIDEO
import org.springframework.hateoas.Link
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder

class VideosLinkBuilder(private val uriComponentsBuilderFactory: UriComponentsBuilderFactory) {
    object Rels {
        const val ASSETS = "assets"
        const val VIDEO = "video"
        const val LOG_VIDEO_INTERACTION = "logInteraction"
        const val SEARCH_VIDEOS = "searchVideos"
        const val TRANSCRIPT = "transcript"
        const val RATE = "rate"
        const val TAG = "tag"
        const val UPDATE = "update"
        const val ADD_ATTACHMENT = "addAttachment"
        const val GET_CAPTIONS = "getCaptions"
        const val UPDATE_CAPTIONS = "updateCaptions"
        const val GET_METADATA = "getMetadata"
    }

    fun self(videoId: String?): HateoasLink {
        val shouldRemoveParams = uriComponentsBuilderFactory
            .getInstance()
            .build()
            .pathSegments.last() == "videos"

        val root = if (shouldRemoveParams) {
            getVideosRootWithoutParams()
        } else {
            getVideosRootWithParams()
        }

        return HateoasLink.of(
            Link.of(
                root.pathSegment(videoId).build().toUriString(),
                "self"
            )
        )
    }

    fun videoLink(): HateoasLink {
        return HateoasLink.of(
            Link.of(
                getVideosRootWithoutParams().pathSegment("{id}").build().toUriString() + "{" +
                    "?referer,shareCode}",
                VIDEO
            )
        )
    }

    fun createVideoInteractedWithEvent(videoId: String?): HateoasLink {
        return HateoasLink.of(
            Link.of(
                getVideosRootWithoutParams()
                    .pathSegment("$videoId")
                    .pathSegment("events")
                    .queryParam("logVideoInteraction", true)
                    .queryParam("type", "{type}")
                    .build()
                    .toUriString(),
                LOG_VIDEO_INTERACTION
            )
        )
    }

    fun searchVideosByText(query: String): HateoasLink? {
        return when {
            currentUserHasRole(UserRoles.VIEW_VIDEOS) -> {
                HateoasLink.of(
                    Link.of(
                        getVideosRootWithoutParams()
                            .queryParam("query", query)
                            .build()
                            .toUriString() +
                            "{" +
                            "&id," +
                            "sort_by," +
                            "duration,duration_facets,duration_min,duration_max," +
                            "released_date_from,released_date_to," +
                            "source," +
                            "age_range_min,age_range_max,age_range,age_range_facets," +
                            "size,page," +
                            "subject,subjects_set_manually," +
                            "promoted," +
                            "content_partner," +
                            "channel," +
                            "type," +
                            "resource_types,resource_type_facets," +
                            "include_channel_facets," +
                            "prices" +
                            "}",
                        SEARCH_VIDEOS
                    )
                )
            }
            else -> null
        }
    }

    fun searchVideosLink(): HateoasLink? {
        return when {
            currentUserHasRole(UserRoles.VIEW_VIDEOS) -> {
                HateoasLink.of(
                    Link.of(
                        getVideosRootWithoutParams()
                            .build()
                            .toUriString() +
                            "{" +
                            "?query,id," +
                            "sort_by," +
                            "duration,duration_facets,duration_min,duration_max," +
                            "released_date_from,released_date_to," +
                            "source," +
                            "age_range_min,age_range_max,age_range,age_range_facets," +
                            "size,page," +
                            "subject,subjects_set_manually," +
                            "promoted,content_partner,type,channel," +
                            "resource_types,resource_type_facets," +
                            "include_channel_facets," +
                            "prices" +
                            "}",
                        SEARCH_VIDEOS
                    )
                )
            }
            else -> null
        }
    }

    fun transcriptLink(video: BaseVideo): HateoasLink? {
        return if (video.hasTranscript()) {
            HateoasLink.of(
                WebMvcLinkBuilder.linkTo(
                    WebMvcLinkBuilder.methodOn(VideoController::class.java)
                        .getTranscript(video.videoId.value)
                ).withRel(Rels.TRANSCRIPT)
            )
        } else {
            null
        }
    }

    fun rateLink(video: BaseVideo): HateoasLink? =
        getIfAuthenticated {
            getIfHasRole(UserRoles.RATE_VIDEOS) {
                HateoasLink.of(
                    Link.of(
                        getVideosRootWithoutParams()
                            .pathSegment(video.videoId.value)
                            .queryParam("rating", "{rating}")
                            .build()
                            .toUriString()
                    ).withRel(Rels.RATE)
                )
            }
        }

    fun tagLink(video: BaseVideo): HateoasLink? {
        return when {
            !currentUserHasRole(UserRoles.TAG_VIDEOS) -> null
            video.tags.isNotEmpty() -> null

            else -> HateoasLink.of(
                WebMvcLinkBuilder.linkTo(
                    WebMvcLinkBuilder.methodOn(VideoController::class.java)
                        .patchUpdateTag(video.videoId.value, null)
                ).withRel(Rels.TAG)
            )
        }
    }

    fun updateLink(video: BaseVideo): HateoasLink? = getIfHasRole(UserRoles.UPDATE_VIDEOS) {
        HateoasLink.of(
            Link.of(
                getVideosRootWithoutParams()
                    .pathSegment(video.videoId.value)
                    .build()
                    .toUriString(),
                UPDATE
            )
        )
    }

    fun updateCaptions(video: Video): HateoasLink? = getIfHasRole(UserRoles.UPDATE_VIDEOS) {
        HateoasLink.of(
            Link.of(
                getVideosRootWithoutParams()
                    .pathSegment(video.videoId.value)
                    .pathSegment("captions")
                    .build()
                    .toUriString(),
                UPDATE_CAPTIONS
            )
        )
    }

    fun getCaptions(): HateoasLink? = getIfHasRole(UserRoles.UPDATE_VIDEOS) {
        getIfAuthenticated {
            getIfHasRole(UserRoles.UPDATE_VIDEOS) {
                HateoasLink.of(
                    Link.of(
                        getVideosRootWithoutParams()
                            .pathSegment("{id}")
                            .pathSegment("captions")
                            .build()
                            .toUriString()
                    ).withRel(GET_CAPTIONS)
                )
            }
        }
    }

    fun addAttachment(video: BaseVideo): HateoasLink? = getIfHasRole(UserRoles.UPDATE_VIDEOS) {
        HateoasLink.of(
            Link.of(
                getVideosRootWithoutParams()
                    .pathSegment(video.videoId.value)
                    .pathSegment("attachments")
                    .build()
                    .toUriString(),
                ADD_ATTACHMENT
            )
        )
    }

    private fun getVideosRootWithoutParams() = uriComponentsBuilderFactory.getInstance()
        .replacePath("/v1/videos")
        .replaceQueryParams(null)

    private fun getVideosRootWithParams() = uriComponentsBuilderFactory.getInstance()
        .replacePath("/v1/videos")

    fun videoDetailsProjection(id: String?) = getIfHasAnyRole(
        UserRoles.LEGACY_PUBLISHER,
        UserRoles.HQ,
        UserRoles.BOCLIPS_SERVICE
    ) {
        HateoasLink.of(
            Link.of(
                getVideosRootWithoutParams().pathSegment(id)
                    .queryParam("projection", Projection.details)
                    .build()
                    .toUriString(),
                "detailsProjection"
            )
        )
    }

    fun videoFullProjection(videoId: String?) = getIfHasAnyRole(
        UserRoles.HQ,
        UserRoles.BOCLIPS_SERVICE
    ) {
        HateoasLink.of(
            Link.of(
                getVideosRootWithoutParams().pathSegment(videoId)
                    .queryParam("projection", Projection.full)
                    .build()
                    .toUriString(),
                "fullProjection"
            )
        )
    }

    fun assets(video: BaseVideo): HateoasLink? =
        takeIf { video.isBoclipsHosted() }?.let {
            getIfHasAnyRole(UserRoles.DOWNLOAD_VIDEO) {
                HateoasLink.of(
                    Link.of(
                        getVideosRootWithoutParams()
                            .pathSegment(video.videoId.value)
                            .pathSegment("assets")
                            .build()
                            .toUriString(),
                        Rels.ASSETS

                    )
                )
            }
        }

    fun getMetadata(): Link? {
        return getIfHasRole(UserRoles.VIEW_VIDEOS) {
            WebMvcLinkBuilder.linkTo(
                WebMvcLinkBuilder.methodOn(VideoController::class.java).getMetadata(null)
            ).withRel(GET_METADATA)
        }
    }
}

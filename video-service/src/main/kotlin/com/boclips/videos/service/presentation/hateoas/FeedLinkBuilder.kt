package com.boclips.videos.service.presentation.hateoas

import com.boclips.security.utils.UserExtractor.currentUserHasRole
import com.boclips.videos.api.response.HateoasLink
import com.boclips.videos.service.config.security.UserRoles
import org.springframework.hateoas.Link

class FeedLinkBuilder(private val uriComponentsBuilderFactory: UriComponentsBuilderFactory) {
    companion object RELS {
        const val NEXT_VIDEO_PAGE = "next"
        const val VIDEOS = "videoFeed"
    }

    fun videos(): HateoasLink? {
        return if (currentUserHasRole(UserRoles.VIEW_VIDEOS)) {
            HateoasLink.of(
                Link.of(
                    root()
                        .pathSegment("videos")
                        .build()
                        .toUriString()
                        .plus("{?size}"),
                    VIDEOS
                )
            )
        } else {
            null
        }
    }

    fun nextVideosPage(cursorId: String?, size: Int): HateoasLink? {
        return cursorId?.let {
            HateoasLink.of(
                Link.of(
                    root()
                        .pathSegment("videos")
                        .queryParam("cursorId", it)
                        .queryParam("size", size)
                        .build()
                        .toUriString(),
                    NEXT_VIDEO_PAGE
                )
            )
        }
    }

    private fun root() = uriComponentsBuilderFactory.getInstance()
        .replacePath("/v1/feed")
        .replaceQueryParams(null)
}

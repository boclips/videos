package com.boclips.videos.service.presentation.hateoas

import com.boclips.videos.api.response.HateoasLink
import com.boclips.videos.service.presentation.hateoas.FeedLinkBuilder.RELS.NEXT_VIDEO_PAGE
import org.springframework.hateoas.Link

class FeedLinkBuilder(private val uriComponentsBuilderFactory: UriComponentsBuilderFactory) {
    private object RELS {
        const val NEXT_VIDEO_PAGE = "next"
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

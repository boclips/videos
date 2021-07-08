package com.boclips.videos.service.presentation

import com.boclips.videos.api.response.video.VideosResource
import com.boclips.videos.api.response.video.VideosWrapperResource
import com.boclips.videos.service.application.video.search.GetVideoFeed
import com.boclips.videos.service.domain.model.video.SearchResultsWithCursor
import com.boclips.videos.service.domain.service.GetUserIdOverride
import com.boclips.videos.service.domain.service.user.AccessRuleService
import com.boclips.videos.service.domain.service.user.UserService
import com.boclips.videos.service.presentation.converters.VideoToResourceConverter
import com.boclips.videos.service.presentation.exceptions.InvalidVideoPaginationException
import com.boclips.videos.service.presentation.hateoas.FeedLinkBuilder
import mu.KLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/feed")
class FeedController(
    private val getVideoFeed: GetVideoFeed,
    private val videoToResourceConverter: VideoToResourceConverter,
    private val feedLinkBuilder: FeedLinkBuilder,
    accessRuleService: AccessRuleService,
    getUserIdOverride: GetUserIdOverride,
    userService: UserService
) : BaseController(accessRuleService, getUserIdOverride, userService) {
    companion object : KLogging() {
        const val DEFAULT_PAGE_SIZE = 1000
        const val MAX_PAGE_SIZE = 1000 // To be decided
    }

    @GetMapping("/videos")
    fun videos(
        @RequestParam size: Int?,
        @RequestParam cursorId: String?
    ): ResponseEntity<VideosResource> {
        val pageSize = size ?: DEFAULT_PAGE_SIZE
        val user = getCurrentUser()

        if (pageSize > MAX_PAGE_SIZE) {
            throw InvalidVideoPaginationException(
                message = "Requested page size is too big. Maximum supported page size is $MAX_PAGE_SIZE"
            )
        }

        val videoFeedResult = getVideoFeed(cursorId, pageSize, user)

        logger.info { "Converting to video feed to resource" }

        val videosResource = VideosResource(
            _embedded = VideosWrapperResource(
                videos = videoToResourceConverter.convert(
                    videoFeedResult.videos,
                    user = user
                ),
                facets = null
            ),
            _links = listOfNotNull(
                generateNextLink(videoFeedResult, pageSize)
            ).associateBy { it.rel }
        )

        return ResponseEntity(videosResource, HttpStatus.OK)
    }

    private fun generateNextLink(
        searchResult: SearchResultsWithCursor,
        pageSize: Int
    ) = if (searchResult.videos.isNotEmpty()) {
        feedLinkBuilder.nextVideosPage(cursorId = searchResult.cursorId, size = pageSize)
    } else {
        null
    }
}

package com.boclips.videos.service.application.video.search

import com.boclips.videos.service.domain.model.user.User
import com.boclips.videos.service.domain.model.video.SearchResultsWithCursor
import com.boclips.videos.service.domain.model.video.request.VideoRequest
import com.boclips.videos.service.domain.model.video.request.VideoRequestPagingState

class GetVideoFeed(
    private val retrievePlayableVideos: RetrievePlayableVideos
) {
    operator fun invoke(
        cursorId: String?,
        size: Int,
        user: User
    ): SearchResultsWithCursor {
        return retrievePlayableVideos.searchPlayableVideosWithCursor(
            request = VideoRequest(
                pageSize = size,
                pagingState = VideoRequestPagingState.Cursor(value = cursorId),
                text = ""
            ),
            videoAccess = user.accessRules.videoAccess
        )
    }
}

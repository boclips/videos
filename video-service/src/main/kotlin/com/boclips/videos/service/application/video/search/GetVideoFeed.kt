package com.boclips.videos.service.application.video.search

import com.boclips.search.service.common.InvalidCursorException
import com.boclips.videos.service.domain.model.user.User
import com.boclips.videos.service.domain.model.video.SearchResultsWithCursor
import com.boclips.videos.service.domain.model.video.request.VideoRequest
import com.boclips.videos.service.domain.model.video.request.VideoRequestPagingState
import com.boclips.web.exceptions.ExceptionDetails
import com.boclips.web.exceptions.InvalidRequestApiException

class GetVideoFeed(
    private val retrievePlayableVideos: RetrievePlayableVideos
) {
    operator fun invoke(
        cursorId: String?,
        size: Int,
        user: User
    ): SearchResultsWithCursor {
        try {
            return retrievePlayableVideos.searchPlayableVideosWithCursor(
                request = VideoRequest(
                    pageSize = size,
                    pagingState = VideoRequestPagingState.Cursor(value = cursorId),
                    text = ""
                ),
                videoAccess = user.accessRules.videoAccess
            )
        } catch (e: InvalidCursorException) {
            throw InvalidRequestApiException(
                exceptionDetails = ExceptionDetails(
                    error = "Invalid cursor id",
                    message = "The cursor id has either timed out or is invalid, please make sure to use the cursor within 5 minutes of the last feed request. You will need to start again with an empty cursor id"
                )
            )
        }
    }
}

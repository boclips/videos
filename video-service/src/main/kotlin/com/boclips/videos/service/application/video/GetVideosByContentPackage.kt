package com.boclips.videos.service.application.video

import com.boclips.videos.service.application.exceptions.ContentPackageNotFoundException
import com.boclips.videos.service.domain.model.PagingCursor
import com.boclips.videos.service.domain.model.contentpackage.ContentPackageId
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.domain.model.video.VideoIdsWithCursor
import com.boclips.videos.service.domain.service.user.ContentPackageService
import com.boclips.videos.service.domain.service.video.VideoRetrievalService

class GetVideosByContentPackage(
    private val videoRetrievalService: VideoRetrievalService,
    private val contentPackageService: ContentPackageService
) {
    operator fun invoke(
        contentPackageId: String,
        pageSize: Int,
        cursorId: String? = null
    ): VideoIdsWithCursor {
        val access = contentPackageService
            .getAccessRules(ContentPackageId(contentPackageId))
            ?: throw ContentPackageNotFoundException(contentPackageId)
        return videoRetrievalService.getVideoIdsWithCursor(
            access.videoAccess,
            pageSize,
            cursorId?.let(::PagingCursor)
        )
    }
}


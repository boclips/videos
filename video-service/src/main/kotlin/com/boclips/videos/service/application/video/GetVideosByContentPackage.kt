package com.boclips.videos.service.application.video

import com.boclips.videos.service.application.channels.VideoChannelService
import com.boclips.videos.service.application.exceptions.ContentPackageNotFoundException
import com.boclips.videos.service.domain.model.PagingCursor
import com.boclips.videos.service.domain.model.contentpackage.ContentPackageId
import com.boclips.videos.service.domain.model.video.VideoIdsWithCursor
import com.boclips.videos.service.domain.service.user.ContentPackageService

class GetVideosByContentPackage(
        private val videoRetrievalService: VideoRetrievalService,
        private val contentPackageService: ContentPackageService,
        private val videoChannelService: VideoChannelService
) {
    operator fun invoke(
        contentPackageId: String,
        pageSize: Int,
        cursorId: String? = null
    ): VideoIdsWithCursor {
        val hiddenChannels = videoChannelService.getHiddenChannelIDs()
        val access = contentPackageService
            .getAccessRules(ContentPackageId(contentPackageId), hiddenChannels)
            ?: throw ContentPackageNotFoundException(contentPackageId)
        return videoRetrievalService.getVideoIdsWithCursor(
            access.videoAccess,
            pageSize,
            cursorId?.let(::PagingCursor)
        )
    }
}


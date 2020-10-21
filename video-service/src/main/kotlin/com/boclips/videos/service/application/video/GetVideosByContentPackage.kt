package com.boclips.videos.service.application.video

import com.boclips.videos.service.application.exceptions.ContentPackageNotFoundException
import com.boclips.videos.service.domain.model.contentpackage.ContentPackageId
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.domain.service.user.ContentPackageService
import com.boclips.videos.service.domain.service.video.VideoRetrievalService

class GetVideosByContentPackage(
    private val videoRetrievalService: VideoRetrievalService,
    private val contentPackageService: ContentPackageService
) {
    operator fun invoke(contentPackageId: String, pageIndex: Int, pageSize: Int): List<VideoId> {
        val access = contentPackageService
            .getAccessRules(ContentPackageId(contentPackageId))
            ?: throw ContentPackageNotFoundException(contentPackageId)
        return videoRetrievalService.getVideoIds(
            pageIndex,
            pageSize,
            access.videoAccess
        )
    }
}


package com.boclips.videos.service.application.video.search

import com.boclips.videos.service.domain.model.contentPartner.ContentPartnerId
import com.boclips.videos.service.domain.service.video.VideoService
import com.boclips.videos.service.presentation.video.VideoResource
import com.boclips.videos.service.presentation.video.VideoToResourceConverter
import org.springframework.hateoas.Resource

class GetAllVideosByContentPartnerId(
    val videoService: VideoService,
    private val videoToResourceConverter: VideoToResourceConverter
) {
    operator fun invoke(contentPartnerId: ContentPartnerId): List<Resource<VideoResource>> {
        return videoService.getPlayableVideos(contentPartnerId)
            .let(videoToResourceConverter::wrapVideosInResource)
    }
}
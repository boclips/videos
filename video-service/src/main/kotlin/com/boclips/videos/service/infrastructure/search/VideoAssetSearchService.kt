package com.boclips.videos.service.infrastructure.search

import com.boclips.search.service.domain.WriteSearchService
import com.boclips.search.service.domain.videos.VideoSearchService
import com.boclips.search.service.domain.videos.VideoSearchServiceAdapter
import com.boclips.search.service.domain.videos.model.VideoMetadata
import com.boclips.videos.service.domain.model.Video
import com.boclips.videos.service.domain.service.video.SearchService

class VideoVideoSearchService(
    videoSearchService: VideoSearchService,
) : VideoSearchServiceAdapter<Video>(videoSearchService) {

    override fun convert(document: Video): VideoMetadata {
        return VideoMetadataConverter.convert(document)
    }
}
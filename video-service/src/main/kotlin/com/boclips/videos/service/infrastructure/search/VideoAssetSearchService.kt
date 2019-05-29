package com.boclips.videos.service.infrastructure.search

import com.boclips.search.service.domain.GenericSearchServiceAdmin
import com.boclips.search.service.domain.videos.VideoSearchServiceAdapter
import com.boclips.search.service.domain.videos.VideoMetadata
import com.boclips.search.service.domain.videos.VideoSearchService
import com.boclips.videos.service.domain.model.Video
import com.boclips.videos.service.domain.service.video.SearchService

class VideoVideoSearchService(
    videoSearchService: VideoSearchService,
    genericSearchServiceAdmin: GenericSearchServiceAdmin<VideoMetadata>
) : VideoSearchServiceAdapter<Video>(videoSearchService, genericSearchServiceAdmin),
    SearchService {

    override fun convert(document: Video): VideoMetadata {
        return VideoMetadataConverter.convert(document)
    }
}
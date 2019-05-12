package com.boclips.videos.service.infrastructure.search

import com.boclips.search.service.domain.GenericSearchService
import com.boclips.search.service.domain.GenericSearchServiceAdmin
import com.boclips.search.service.domain.SearchServiceAdapter
import com.boclips.search.service.domain.VideoMetadata
import com.boclips.videos.service.domain.model.Video
import com.boclips.videos.service.domain.service.video.SearchService

class VideoSearchService(
    genericSearchService: GenericSearchService,
    genericSearchServiceAdmin: GenericSearchServiceAdmin<VideoMetadata>
) : SearchServiceAdapter<Video>(genericSearchService, genericSearchServiceAdmin),
    SearchService {

    override fun convert(document: Video): VideoMetadata {
        return VideoMetadataConverter.convert(document)
    }
}
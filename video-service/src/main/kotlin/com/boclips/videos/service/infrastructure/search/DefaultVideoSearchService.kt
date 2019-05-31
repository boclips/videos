package com.boclips.videos.service.infrastructure.search

import com.boclips.search.service.domain.ReadSearchService
import com.boclips.search.service.domain.WriteSearchService
import com.boclips.search.service.domain.videos.VideoSearchServiceAdapter
import com.boclips.search.service.domain.videos.model.VideoMetadata
import com.boclips.search.service.domain.videos.model.VideoQuery
import com.boclips.videos.service.domain.model.Video
import com.boclips.videos.service.domain.service.video.VideoSearchService

class DefaultVideoSearchService(
    readSearchService: ReadSearchService<VideoMetadata, VideoQuery>,
    writeSearchService: WriteSearchService<VideoMetadata>
) : VideoSearchServiceAdapter<Video>(readSearchService, writeSearchService),
    VideoSearchService {

    override fun convert(document: Video): VideoMetadata {
        return VideoMetadataConverter.convert(document)
    }
}
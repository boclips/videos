package com.boclips.videos.service.infrastructure.search

import com.boclips.search.service.domain.common.IndexReader
import com.boclips.search.service.domain.common.IndexWriter
import com.boclips.search.service.domain.videos.VideoSearchAdapter
import com.boclips.search.service.domain.videos.model.VideoMetadata
import com.boclips.search.service.domain.videos.model.VideoQuery
import com.boclips.videos.service.domain.model.video.Video
import com.boclips.videos.service.domain.service.ContentPartnerService
import com.boclips.videos.service.domain.service.video.VideoSearchService

class DefaultVideoSearch(
    indexReader: IndexReader<VideoMetadata, VideoQuery>,
    indexWriter: IndexWriter<VideoMetadata>,
    private val contentPartnerService: ContentPartnerService
) : VideoSearchAdapter<Video>(indexReader, indexWriter),
    VideoSearchService {

    override fun convert(video: Video): VideoMetadata {
        val videoAvailability = contentPartnerService.findAvailabilityFor(video.contentPartner.contentPartnerId)

        return VideoMetadataConverter.convert(video = video, videoAvailability = videoAvailability)
    }
}

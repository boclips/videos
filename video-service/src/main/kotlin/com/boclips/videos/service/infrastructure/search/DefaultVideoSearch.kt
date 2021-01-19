package com.boclips.videos.service.infrastructure.search

import com.boclips.search.service.domain.common.IndexReader
import com.boclips.search.service.domain.common.IndexWriter
import com.boclips.search.service.domain.videos.VideoSearchAdapter
import com.boclips.search.service.domain.videos.model.VideoMetadata
import com.boclips.search.service.domain.videos.model.VideoQuery
import com.boclips.videos.service.domain.model.video.Video
import com.boclips.videos.service.domain.model.video.prices.VideoWithPrices
import com.boclips.videos.service.domain.service.VideoChannelService
import com.boclips.videos.service.domain.service.video.VideoIndex

class DefaultVideoSearch(
    indexReader: IndexReader<VideoMetadata, VideoQuery>,
    indexWriter: IndexWriter<VideoMetadata>,
    private val videoChannelService: VideoChannelService
) : VideoSearchAdapter<VideoWithPrices>(indexReader, indexWriter),
    VideoIndex {

    override fun convert(document: VideoWithPrices): VideoMetadata {
        val videoAvailability = videoChannelService.findAvailabilityFor(document.channel.channelId)
TODO("use vid w prices")
//        return VideoMetadataConverter.convert(video = document, videoAvailability = videoAvailability)
    }
}

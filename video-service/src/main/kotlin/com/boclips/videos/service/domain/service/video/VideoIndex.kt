package com.boclips.videos.service.domain.service.video

import com.boclips.search.service.domain.common.IndexReader
import com.boclips.search.service.domain.common.IndexWriter
import com.boclips.search.service.domain.videos.model.VideoMetadata
import com.boclips.search.service.domain.videos.model.VideoQuery
import com.boclips.videos.service.domain.model.video.prices.VideoWithPrices

interface VideoIndex : IndexReader<VideoMetadata, VideoQuery>, IndexWriter<VideoWithPrices>

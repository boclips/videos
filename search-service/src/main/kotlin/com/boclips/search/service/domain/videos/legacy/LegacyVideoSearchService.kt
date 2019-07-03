package com.boclips.search.service.domain.videos.legacy

import com.boclips.search.service.domain.common.IndexReader
import com.boclips.search.service.domain.common.IndexWriter
import com.boclips.search.service.domain.videos.model.VideoMetadata
import com.boclips.search.service.domain.videos.model.VideoQuery

interface LegacyVideoSearchService : IndexReader<VideoMetadata, VideoQuery>, IndexWriter<LegacyVideoMetadata>
package com.boclips.search.service.domain.legacy

import com.boclips.search.service.domain.ReadSearchService
import com.boclips.search.service.domain.WriteSearchService
import com.boclips.search.service.domain.videos.model.VideoMetadata
import com.boclips.search.service.domain.videos.model.VideoQuery

interface LegacySearchService : ReadSearchService<VideoMetadata, VideoQuery>, WriteSearchService<LegacyVideoMetadata>
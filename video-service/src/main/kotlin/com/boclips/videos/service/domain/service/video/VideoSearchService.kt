package com.boclips.videos.service.domain.service.video

import com.boclips.search.service.domain.ReadSearchService
import com.boclips.search.service.domain.WriteSearchService
import com.boclips.search.service.domain.videos.model.VideoMetadata
import com.boclips.search.service.domain.videos.model.VideoQuery
import com.boclips.videos.service.domain.model.Video

interface VideoSearchService :  ReadSearchService<VideoMetadata, VideoQuery>, WriteSearchService<Video>
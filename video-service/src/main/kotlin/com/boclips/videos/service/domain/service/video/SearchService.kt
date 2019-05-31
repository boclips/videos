package com.boclips.videos.service.domain.service.video

import com.boclips.search.service.domain.videos.VideoSearchService
import com.boclips.search.service.domain.WriteSearchService
import com.boclips.videos.service.domain.model.Video

interface SearchService : VideoSearchService, WriteSearchService<Video>
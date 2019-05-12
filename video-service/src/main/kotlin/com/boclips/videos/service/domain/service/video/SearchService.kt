package com.boclips.videos.service.domain.service.video

import com.boclips.search.service.domain.GenericSearchService
import com.boclips.search.service.domain.GenericSearchServiceAdmin
import com.boclips.videos.service.domain.model.Video

interface SearchService : GenericSearchService, GenericSearchServiceAdmin<Video>
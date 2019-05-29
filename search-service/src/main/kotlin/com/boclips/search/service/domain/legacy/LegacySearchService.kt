package com.boclips.search.service.domain.legacy

import com.boclips.search.service.domain.videos.VideoSearchService
import com.boclips.search.service.domain.GenericSearchServiceAdmin

interface LegacySearchService : VideoSearchService, GenericSearchServiceAdmin<LegacyVideoMetadata>
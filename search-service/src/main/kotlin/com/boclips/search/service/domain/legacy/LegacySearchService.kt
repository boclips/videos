package com.boclips.search.service.domain.legacy

import com.boclips.search.service.domain.GenericSearchService
import com.boclips.search.service.domain.GenericSearchServiceAdmin

interface LegacySearchService : GenericSearchService, GenericSearchServiceAdmin<LegacyVideoMetadata>
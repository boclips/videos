package com.boclips.search.service.domain.common

import com.boclips.search.service.domain.common.model.PagingCursor

class SearchResults(val elements: List<String>, val counts: ResultCounts, val cursor: PagingCursor? = null)

package com.boclips.search.service.domain.channels

import com.boclips.search.service.domain.common.model.SearchQuery
import com.boclips.search.service.domain.common.model.SearchRequestWithoutPagination

interface IndexReader<M, Q : SearchQuery<M>> {
    fun search(searchRequest: SearchRequestWithoutPagination<Q>): SearchChannelsResults
}

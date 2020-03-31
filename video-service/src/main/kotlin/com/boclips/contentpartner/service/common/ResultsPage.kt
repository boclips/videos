package com.boclips.contentpartner.service.common

import kotlin.math.ceil

data class ResultsPage<E>(val elements: Iterable<E>, val pageInfo: PageInfo)

data class PageInfo(val hasMoreElements: Boolean, val totalElements: Long, val pageRequest: PageRequest) {
    val totalPages: Long by lazy { ceil(totalElements.toDouble() / pageRequest.size.toDouble()).toLong() }
}



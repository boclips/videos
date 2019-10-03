package com.boclips.videos.service.common

data class Page<E>(val elements: Iterable<E>, val pageInfo: PageInfo)

data class PageInfo(val hasMoreElements: Boolean, val totalElements: Long, val pageRequest: PageRequest)


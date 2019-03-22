package com.boclips.videos.service.domain.model

data class Page<E>(val elements: Iterable<E>, val pageInfo: PageInfo)

data class PageInfo(val hasMoreElements: Boolean)


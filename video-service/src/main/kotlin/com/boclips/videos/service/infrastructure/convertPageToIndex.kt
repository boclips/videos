package com.boclips.videos.service.infrastructure

fun convertPageToIndex(pageSize: Int, pageIndex: Int): Int {
    return pageSize * pageIndex
}

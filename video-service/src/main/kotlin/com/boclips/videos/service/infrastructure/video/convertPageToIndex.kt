package com.boclips.videos.service.infrastructure.video

fun convertPageToIndex(pageSize: Int, pageIndex: Int): Int {
    return pageSize * pageIndex
}
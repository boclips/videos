package com.boclips.search.service.domain.videos.model

data class VideoCategoryCodes(val codes: List<String>) : Comparable<VideoCategoryCodes> {
    override fun compareTo(other: VideoCategoryCodes): Int {
        TODO("Not yet implemented")
    }
}


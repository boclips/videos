package com.boclips.search.service.domain.videos.model

data class VideoCategoryCodes(val codes: List<String>) : Comparable<VideoCategoryCodes> {
    override fun compareTo(other: VideoCategoryCodes): Int {
        return this.getSortPriority().compareTo(other.getSortPriority())
    }

    private fun getSortPriority(): String {
        return if (this.codes.isEmpty()) {
            "00"
        } else {
            "01"
        }
    }
}


package com.boclips.videos.service.domain.model

class VideoId(
        val videoId: String
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as VideoId

        if (videoId != other.videoId) return false

        return true
    }

    override fun hashCode(): Int {
        return videoId.hashCode()
    }

    override fun toString(): String {
        return "[id = ${this.videoId}]"
    }

}

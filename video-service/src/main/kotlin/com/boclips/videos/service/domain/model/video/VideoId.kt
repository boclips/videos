package com.boclips.videos.service.domain.model.video

import org.bson.types.ObjectId
import com.boclips.eventbus.domain.video.VideoId as EventVideoId

class VideoId(value: String) {
    val value: String

    init {
        try {
            val cleanedId = value.replace(Regex("\uFEFF"), "")
            ObjectId(cleanedId)
            this.value = cleanedId
        } catch (e: IllegalArgumentException) {
            throw IllegalVideoIdentifierException("$value is not a valid ID")
        }
    }

    override fun toString(): String {
        return this.value
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as VideoId

        if (value != other.value) return false

        return true
    }

    override fun hashCode(): Int {
        return value.hashCode()
    }

    fun toEvent() : EventVideoId {
        return EventVideoId.of(value)
    }
}

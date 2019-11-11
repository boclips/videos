package com.boclips.videos.service.domain.model.video

enum class LegacyVideoType(val id: Int, val title: String) {
    NEWS(1, "News"),
    STOCK(2, "Stock"),
    INSTRUCTIONAL_CLIPS(3, "Instructional Clips");

    companion object {
        private val typeById = values().map { it.id to it }.toMap()

        fun fromId(id: Int): LegacyVideoType {
            return typeById[id] ?: throw IllegalArgumentException("The type id $id is invalid")
        }
    }
}

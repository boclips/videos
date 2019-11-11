package com.boclips.videos.service.domain.model.video

enum class ContentType(val id: Int, val title: String) {
    NEWS(1, "News"),
    STOCK(2, "Stock"),
    INSTRUCTIONAL_CLIPS(3, "Instructional Clips");

    companion object {
        private val typeById = values().map { it.id to it }.toMap()

        fun fromId(id: Int): ContentType {
            return typeById[id] ?: throw IllegalArgumentException("The type id $id is invalid")
        }
    }
}

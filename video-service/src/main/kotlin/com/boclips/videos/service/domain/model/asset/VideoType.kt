package com.boclips.videos.service.domain.model.asset

enum class VideoType(val id: Int) {
    OTHER(0),
    NEWS(1),
    STOCK(2),
    INSTRUCTIONAL_CLIPS(3),
    TV_CLIPS(4),
    NEWS_PACKAGE(5),
    UGC_NEWS(6),
    VR_360_STOCK(7),
    VR_360_IMMERSIVE(8),
    SHORT_PROGRAMME(9),
    TED_TALKS(10),
    TED_ED(11);

    companion object {

        private val typeById = VideoType.values().map { it.id to it }.toMap()

        fun fromId(id: Int): VideoType {
            return typeById[id] ?: throw IllegalArgumentException("The type id $id is invalid")
        }
    }
}
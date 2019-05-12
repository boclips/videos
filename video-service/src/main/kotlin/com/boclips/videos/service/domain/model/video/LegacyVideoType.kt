package com.boclips.videos.service.domain.model.video

enum class LegacyVideoType(val id: Int, val title: String) {
    OTHER(0, "Other"),
    NEWS(1, "News"),
    STOCK(2, "Stock"),
    INSTRUCTIONAL_CLIPS(3, "Instructional Clips"),
    TV_CLIPS(4, "TV Clips"),
    NEWS_PACKAGE(5, "News Package"),
    UGC_NEWS(6, "UGC News"),
    VR_360_STOCK(7, "360 VR Stock"),
    VR_360_IMMERSIVE(8, "360 VR Immersive"),
    SHORT_PROGRAMME(9, "Short Programme"),
    TED_TALKS(10, "TED Talks"),
    TED_ED(11, "TED-Ed");

    companion object {
        private val typeById = LegacyVideoType.values().map { it.id to it }.toMap()

        fun fromId(id: Int): LegacyVideoType {
            return typeById[id] ?: throw IllegalArgumentException("The type id $id is invalid")
        }
    }
}
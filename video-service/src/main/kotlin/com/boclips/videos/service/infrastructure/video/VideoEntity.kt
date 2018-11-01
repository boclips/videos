package com.boclips.videos.service.infrastructure.video

import com.boclips.videos.service.domain.model.Video
import com.boclips.videos.service.domain.model.VideoId
import com.boclips.videos.service.domain.model.VideoType
import org.springframework.data.annotation.Id
import java.time.LocalDate

class VideoEntity(
        @Id var id: Long,
        var source: String? = null,
        var namespace: String? = null,
        var title: String? = null,
        var description: String? = null,
        var date: String? = null,
        var duration: String? = null,
        var keywords: String? = null,
        var price_category: String? = null,
        var sounds: String? = null,
        var color: String? = null,
        var location: String? = null,
        var country: String? = null,
        var state: String? = null,
        var city: String? = null,
        var region: String? = null,
        var alternative_id: String? = null,
        var alt_source: String? = null,
        var restrictions: String? = null,
        var type_id: Int? = null,
        var reference_id: String? = null
) {

    fun toVideo(): Video {
        return Video(
                videoId = VideoId(videoId = id.toString(), referenceId = reference_id),
                title = title!!,
                description = description!!,
                releasedOn = LocalDate.parse(date!!),
                contentProvider = source!!,
                videoPlayback = null,
                type = getVideoType(),
                keywords = keywords?.split(",")?.map { it.trim() }.orEmpty()
        )
    }

    private fun getVideoType(): VideoType {
        return when (type_id) {
            0 -> VideoType.OTHER
            1 -> VideoType.NEWS
            2 -> VideoType.STOCK
            3 -> VideoType.INSTRUCTIONAL_CLIPS
            4 -> VideoType.TV_CLIPS
            5 -> VideoType.NEWS_PACKAGE
            6 -> VideoType.UGC_NEWS
            7 -> VideoType.VR_360_STOCK
            8 -> VideoType.VR_360_IMMERSIVE
            9 -> VideoType.SHORT_PROGRAMME
            10 -> VideoType.TED_TALKS
            11 -> VideoType.TED_ED
            else -> throw IllegalStateException("Unknown type_id: $type_id")
        }
    }

}

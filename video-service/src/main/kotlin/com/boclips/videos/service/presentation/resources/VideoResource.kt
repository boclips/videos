package com.boclips.videos.service.presentation.resources

import com.boclips.videos.service.domain.model.Video
import org.springframework.hateoas.core.Relation
import java.time.Duration
import java.time.LocalDate

@Relation(collectionRelation = "videos")
data class VideoResource(
        val title: String? = null,
        val description: String? = null,
        val duration: Duration? = null,
        val releasedOn: LocalDate? = null,
        val contentProvider: String? = null,
        val streamUrl: String? = null,
        val thumbnailUrl: String? = null
) {
    companion object {
        fun from(video: Video): VideoResource {
            return VideoResource(
                    title = video.title,
                    description = video.description,
                    contentProvider = video.contentProvider,
                    releasedOn = video.releasedOn,
                    duration = video.duration,
                    streamUrl = video.streamUrl,
                    thumbnailUrl = video.thumbnailUrl
            )
        }
    }
}
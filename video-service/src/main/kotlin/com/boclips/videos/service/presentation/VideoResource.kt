package com.boclips.videos.service.presentation

import com.boclips.videos.service.domain.model.Video
import org.springframework.hateoas.core.Relation

@Relation(collectionRelation = "videos")
data class VideoResource(
        val title: String? = null,
        val description: String? = null
) {
    companion object {
        fun from(video: Video): VideoResource {
            return VideoResource(
                    title = video.title,
                    description = video.description
            )
        }
    }
}
package com.boclips.videos.service.infrastructure.video

import java.io.Serializable
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.IdClass

data class VideoSubjectId(
        val videoId: Long? = null,
        val subjectName: String? = null
) : Serializable

@Entity(name = "video_subject")
@IdClass(VideoSubjectId::class)
data class VideoSubject(
        @Id val videoId: Long? = null,
        @Id val subjectName: String? = null
)
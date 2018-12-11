package com.boclips.videos.service.infrastructure.video

import java.io.Serializable
import javax.persistence.*


data class VideoSubjectId(
        val video: VideoEntity? = null,
        val subjectName: String? = null
) : Serializable

@Entity(name = "video_subject")
@IdClass(VideoSubjectId::class)
data class VideoSubject(
        @Id val subjectName: String? = null,

        @ManyToOne
        @JoinColumn(name = "video_id")
        val video: VideoEntity? = null
)
package com.boclips.videos.service.infrastructure.video.subject

import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.IdClass

@Entity(name = "video_subject")
@IdClass(VideoSubjectId::class)
data class VideoSubjectEntity(
        @Id val videoId: Long? = null,
        @Id val subjectName: String? = null
)
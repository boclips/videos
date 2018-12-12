package com.boclips.videos.service.infrastructure.video.subject

import java.io.Serializable

data class VideoSubjectId(
        val videoId: Long? = null,
        val subjectName: String? = null
) : Serializable
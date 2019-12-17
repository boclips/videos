package com.boclips.videos.service.domain.model.video

import com.boclips.videos.service.domain.model.subject.Subject

data class VideoSubjects(
    val setManually: Boolean?,
    val items: Set<Subject>
)

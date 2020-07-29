package com.boclips.videos.service.domain.model

import com.boclips.videos.service.domain.model.subject.Subject

class Suggestions(
    val channels: List<String> = emptyList(),
    val subjects: List<Subject> = emptyList()
)

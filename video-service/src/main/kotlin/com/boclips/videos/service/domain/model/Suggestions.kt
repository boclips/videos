package com.boclips.videos.service.domain.model

import com.boclips.contentpartner.service.domain.model.channel.Channel
import com.boclips.videos.service.domain.model.subject.Subject

class Suggestions(
    val channels: List<Channel> = emptyList(),
    val subjects: List<Subject> = emptyList()
)

package com.boclips.videos.service.domain.model.suggestions

import com.boclips.contentpartner.service.domain.model.channel.ChannelId
import com.boclips.videos.service.domain.model.subject.SubjectId

data class NewSuggestions(
    val channels: List<ChannelSuggestion>,
    val subjects: List<SubjectSuggestion>
)

data class ChannelSuggestion(
    val name: String,
    val id: ChannelId
)

data class SubjectSuggestion(
    val name: String,
    val id: SubjectId
)

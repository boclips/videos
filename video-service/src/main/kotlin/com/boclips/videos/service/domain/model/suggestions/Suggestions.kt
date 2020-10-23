package com.boclips.videos.service.domain.model.suggestions

import com.boclips.contentpartner.service.domain.model.channel.ChannelId
import com.boclips.search.service.domain.channels.model.ContentType
import com.boclips.videos.service.domain.model.subject.SubjectId

data class Suggestions(
    val channels: List<ChannelSuggestion>,
    val subjects: List<SubjectSuggestion>
)

data class ChannelSuggestion(
    val name: String,
    val id: ChannelId,
    val eligibleForStream: Boolean,
    val contentTypes: List<ContentType>
)

data class SubjectSuggestion(
    val name: String,
    val id: SubjectId
)

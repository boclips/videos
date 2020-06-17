package com.boclips.videos.service.infrastructure.video

data class SourceDocument(
    val channel: ChannelDocument,
    val videoReference: String
)

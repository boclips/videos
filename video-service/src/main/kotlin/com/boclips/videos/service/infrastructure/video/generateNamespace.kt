package com.boclips.videos.service.infrastructure.video

fun generateNamespace(contentProvider: String, videoId: String): String {
    return "${contentProvider.replace(" ", "")}:$videoId"
}
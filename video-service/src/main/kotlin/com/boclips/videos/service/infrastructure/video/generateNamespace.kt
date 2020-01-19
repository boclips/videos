package com.boclips.videos.service.infrastructure.video

fun generateNamespace(contentProvider: String, contentProviderId: String): String {
    return "${contentProvider.replace(" ", "")}:$contentProviderId"
}

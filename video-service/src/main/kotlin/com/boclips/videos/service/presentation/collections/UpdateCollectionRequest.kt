package com.boclips.videos.service.presentation.collections

class UpdateCollectionRequest(
    var title: String? = null,
    var isPublic: Boolean? = null,
    var subjects: Set<String>? = null
)
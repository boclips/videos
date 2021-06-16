package com.boclips.videos.service.domain.model.video

import java.net.URI

data class DownloadableCaption(
    val downloadUrl: URI,
    val format: CaptionFormat
)

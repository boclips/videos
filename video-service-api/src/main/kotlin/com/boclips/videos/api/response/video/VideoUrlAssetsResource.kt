package com.boclips.videos.api.response.video

import com.fasterxml.jackson.annotation.JsonInclude

data class VideoUrlAssetsResource(
    val downloadVideoUrl: String?,

    @JsonInclude(JsonInclude.Include.NON_NULL)
    val downloadCaptionUrl: String?,

    @JsonInclude(JsonInclude.Include.NON_NULL)
    val captionFileExtension: String?
)
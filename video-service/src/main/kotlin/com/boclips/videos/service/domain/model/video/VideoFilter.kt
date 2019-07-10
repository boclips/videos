package com.boclips.videos.service.domain.model.video

sealed class VideoFilter {
    data class ContentPartnerIs(val contentPartnerName: String) : VideoFilter()
    data class LegacyTypeIs(val type: LegacyVideoType) : VideoFilter()
    object IsYoutube : VideoFilter()
    object IsKaltura : VideoFilter()
    object IsDownloadable : VideoFilter()
    object IsStreamable : VideoFilter()
}

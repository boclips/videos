package com.boclips.videos.service.domain.model.video

sealed class VideoFilter {
    data class ContentPartnerIs(val contentPartnerName: String) : VideoFilter()
    object IsSearchable : VideoFilter()
    object IsYoutube : VideoFilter()
    object IsKaltura : VideoFilter()
}

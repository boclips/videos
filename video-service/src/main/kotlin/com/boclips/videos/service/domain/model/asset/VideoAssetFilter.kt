package com.boclips.videos.service.domain.model.asset

sealed class VideoAssetFilter {
    data class ContentPartnerIs(val contentPartnerName: String) : VideoAssetFilter()
    object IsSearchable : VideoAssetFilter()
}

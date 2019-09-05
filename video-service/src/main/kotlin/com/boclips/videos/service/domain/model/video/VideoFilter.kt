package com.boclips.videos.service.domain.model.video

import com.boclips.videos.service.domain.model.contentPartner.ContentPartnerId

sealed class VideoFilter {
    data class ContentPartnerNameIs(val contentPartnerName: String) : VideoFilter()
    data class ContentPartnerIdIs(val contentPartnerId: ContentPartnerId) : VideoFilter()
    data class LegacyTypeIs(val type: LegacyVideoType) : VideoFilter()
    object IsYoutube : VideoFilter()
    object IsKaltura : VideoFilter()
    object IsDownloadable : VideoFilter()
    object IsStreamable : VideoFilter()
}

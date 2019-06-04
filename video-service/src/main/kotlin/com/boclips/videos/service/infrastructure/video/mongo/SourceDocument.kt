package com.boclips.videos.service.infrastructure.video.mongo

import com.boclips.videos.service.infrastructure.contentPartner.ContentPartnerDocument

data class SourceDocument(
    val contentPartner: ContentPartnerDocument,
    val videoReference: String
)
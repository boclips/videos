package com.boclips.videos.service.infrastructure.video

import com.boclips.contentpartner.service.infrastructure.ContentPartnerDocument

data class SourceDocument(
    val contentPartner: ContentPartnerDocument,
    val videoReference: String
)

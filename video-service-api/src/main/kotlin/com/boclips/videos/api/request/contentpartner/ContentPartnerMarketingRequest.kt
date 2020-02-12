package com.boclips.videos.api.request.contentpartner

enum class ContentPartnerStatusRequest {
    NeedsIntroduction,
    HaveReachedOut,
    NeedsContent,
    WaitingForIngest,
    ShouldAddToSite,
    ShouldPromote,
    Promoted
}

data class ContentPartnerMarketingRequest(
    val status: ContentPartnerStatusRequest
)
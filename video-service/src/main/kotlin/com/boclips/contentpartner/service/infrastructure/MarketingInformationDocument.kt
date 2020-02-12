package com.boclips.contentpartner.service.infrastructure

data class MarketingInformationDocument(
    val oneLineDescription: String?,
    val status: ContentPartnerStatusDocument?
)

enum class ContentPartnerStatusDocument {
    NeedsIntroduction,
    HaveReachedOut,
    NeedsContent,
    WaitingForIngest,
    ShouldAddToSite,
    ShouldPromote,
    Promoted
}

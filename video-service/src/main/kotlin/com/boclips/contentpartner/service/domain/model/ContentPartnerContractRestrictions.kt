package com.boclips.contentpartner.service.domain.model

data class ContentPartnerContractRestrictions(
    val clientFacing: List<String>,
    val territory: String?,
    val licensing: String?,
    val editing:String?,
    val marketing:String?,
    val companies: String?,
    val payout: String?,
    val other: String?
)
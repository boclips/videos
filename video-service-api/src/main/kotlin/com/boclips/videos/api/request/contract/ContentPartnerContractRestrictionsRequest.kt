package com.boclips.videos.api.request.contract

import com.fasterxml.jackson.annotation.JsonSetter
import com.fasterxml.jackson.annotation.Nulls

data class ContentPartnerContractRestrictionsRequest(
    @JsonSetter(contentNulls = Nulls.FAIL)
    val clientFacing: List<String>?,
    val territory: String?,
    val licensing: String?,
    val editing:String?,
    val marketing:String?,
    val companies: String?,
    val payout: String?,
    val other: String?
)
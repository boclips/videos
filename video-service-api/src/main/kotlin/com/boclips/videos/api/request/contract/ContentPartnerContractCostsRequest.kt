package com.boclips.videos.api.request.contract

import java.math.BigDecimal

data class ContentPartnerContractCostsRequest(
    val minimumGuarantee: List<BigDecimal> = emptyList(),
    val upfrontLicense: BigDecimal?,
    val technicalFee: BigDecimal?,
    val recoupable: Boolean?
)
package com.boclips.videos.api.response.contract

import java.math.BigDecimal

data class ContractCostsResource(
    val minimumGuarantee: List<BigDecimal> = emptyList(),
    val upfrontLicense: BigDecimal?,
    val technicalFee: BigDecimal?,
    val recoupable: Boolean?
)

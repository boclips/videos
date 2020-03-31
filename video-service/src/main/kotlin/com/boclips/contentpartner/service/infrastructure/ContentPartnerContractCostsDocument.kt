package com.boclips.contentpartner.service.infrastructure

import java.math.BigDecimal

data class ContentPartnerContractCostsDocument(
    val minimumGuarantee: List<BigDecimal> = emptyList(),
    val upfrontLicense: BigDecimal?,
    val technicalFee: BigDecimal?,
    val recoupable: Boolean?
)
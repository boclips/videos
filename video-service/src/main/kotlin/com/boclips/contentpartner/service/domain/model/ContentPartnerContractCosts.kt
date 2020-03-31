package com.boclips.contentpartner.service.domain.model

import java.math.BigDecimal

data class ContentPartnerContractCosts(
    val minimumGuarantee: List<BigDecimal> = emptyList(),
    val upfrontLicense: BigDecimal?,
    val technicalFee: BigDecimal?,
    val recoupable: Boolean?
)
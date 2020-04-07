package com.boclips.contentpartner.service.infrastructure.contract

import java.math.BigDecimal

data class ContractCostsDocument(
    val minimumGuarantee: List<BigDecimal> = emptyList(),
    val upfrontLicense: BigDecimal?,
    val technicalFee: BigDecimal?,
    val recoupable: Boolean?
)

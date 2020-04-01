package com.boclips.contentpartner.service.infrastructure.contentpartnercontract

import java.math.BigDecimal

data class ContractCostsDocument(
    val minimumGuarantee: List<BigDecimal> = emptyList(),
    val upfrontLicense: BigDecimal?,
    val technicalFee: BigDecimal?,
    val recoupable: Boolean?
)

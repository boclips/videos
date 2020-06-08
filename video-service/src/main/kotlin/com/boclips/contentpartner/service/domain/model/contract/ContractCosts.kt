package com.boclips.contentpartner.service.domain.model.contract

import java.math.BigDecimal

data class ContractCosts(
    val minimumGuarantee: List<BigDecimal> = emptyList(),
    val upfrontLicense: BigDecimal?,
    val technicalFee: BigDecimal?,
    val recoupable: Boolean?
)

package com.boclips.videos.api.request.contract

import com.fasterxml.jackson.annotation.JsonSetter
import com.fasterxml.jackson.annotation.Nulls
import java.math.BigDecimal

data class ContentPartnerContractCostsRequest(
    @JsonSetter(contentNulls = Nulls.FAIL)
    val minimumGuarantee: List<BigDecimal> = emptyList(),
    val upfrontLicense: BigDecimal?,
    val technicalFee: BigDecimal?,
    val recoupable: Boolean?
)
package com.boclips.contentpartner.service.domain.model.channel

import com.boclips.contentpartner.service.domain.model.agerange.AgeRangeBuckets

data class PedagogyInformation(
    val bestForTags: List<String>? = null,
    val subjects: List<String>? = null,
    val ageRangeBuckets: AgeRangeBuckets? = null
)

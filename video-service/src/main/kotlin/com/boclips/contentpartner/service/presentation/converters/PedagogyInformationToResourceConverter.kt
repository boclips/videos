package com.boclips.contentpartner.service.presentation.converters

import com.boclips.contentpartner.service.domain.model.channel.PedagogyInformation
import com.boclips.contentpartner.service.presentation.ageRange.AgeRangeToResourceConverter
import com.boclips.videos.api.response.channel.PedagogyResource

object PedagogyInformationToResourceConverter {
    fun from(pedagogyInformation: PedagogyInformation?) =
        pedagogyInformation?.let {
            PedagogyResource(
                bestForTags = pedagogyInformation.bestForTags,
                subjects = pedagogyInformation.subjects,
                ageRanges = pedagogyInformation.ageRangeBuckets?.let { AgeRangeToResourceConverter.convert(it) }
            )
        }
}

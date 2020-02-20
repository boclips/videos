package com.boclips.contentpartner.service.presentation

import com.boclips.contentpartner.service.domain.model.PedagogyInformation
import com.boclips.contentpartner.service.presentation.ageRange.AgeRangeToResourceConverter
import com.boclips.videos.api.response.contentpartner.ContentPartnerPedagogyResource

object PedagogyInformationToResourceConverter {
    fun from(pedagogyInformation: PedagogyInformation?) =
        pedagogyInformation?.let {
            ContentPartnerPedagogyResource(
                isTranscriptProvided = pedagogyInformation.isTranscriptProvided,
                curriculumAligned = pedagogyInformation.curriculumAligned,
                bestForTags = pedagogyInformation.bestForTags,
                subjects = pedagogyInformation.subjects,
                educationalResources = pedagogyInformation.educationalResources,
                ageRanges = AgeRangeToResourceConverter.convert(pedagogyInformation.ageRangeBuckets!!)
            )
        }

}
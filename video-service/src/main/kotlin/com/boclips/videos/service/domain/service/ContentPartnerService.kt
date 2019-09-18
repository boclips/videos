package com.boclips.videos.service.domain.service

import com.boclips.contentpartner.service.application.ContentPartnerNotFoundException
import com.boclips.contentpartner.service.application.GetContentPartner
import com.boclips.contentpartner.service.domain.model.ContentPartnerId
import com.boclips.contentpartner.service.domain.model.Credit
import com.boclips.videos.service.domain.model.common.AgeRange
import com.boclips.videos.service.domain.model.video.ContentPartner
import com.boclips.videos.service.domain.model.video.DistributionMethod
import com.boclips.web.exceptions.ResourceNotFoundApiException
import org.springframework.stereotype.Component

@Component
class ContentPartnerService(val getContentPartner: GetContentPartner) {

    fun findById(id: String): ContentPartner? {
        try {

            val contentPartner = getContentPartner(id)
            return ContentPartner(
                contentPartnerId = ContentPartnerId(contentPartner.id),
                name = contentPartner.name,
                ageRange = AgeRange.bounded(
                    contentPartner.ageRange?.min,
                    contentPartner.ageRange?.max
                ),
                credit = Credit.PartnerCredit,
                legalRestrictions = null,
                distributionMethods = contentPartner.distributionMethods.map {
                    DistributionMethod.valueOf(
                        it.name
                    )
                }.toSet()
            )
        } catch(e: ResourceNotFoundApiException) {
            throw ContentPartnerNotFoundException(id)
        }
    }
}

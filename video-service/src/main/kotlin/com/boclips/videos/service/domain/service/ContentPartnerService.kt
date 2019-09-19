package com.boclips.videos.service.domain.service

import com.boclips.contentpartner.service.application.ContentPartnerNotFoundException
import com.boclips.contentpartner.service.domain.model.ContentPartnerId
import com.boclips.contentpartner.service.domain.model.ContentPartnerRepository
import com.boclips.videos.service.domain.model.common.AgeRange
import com.boclips.videos.service.domain.model.video.ContentPartner
import com.boclips.videos.service.domain.model.video.DistributionMethod
import org.springframework.stereotype.Component

@Component
class ContentPartnerService(val contentPartnerRepository: ContentPartnerRepository) {

    fun findById(id: String): ContentPartner? {
        val contentPartner = find(ContentPartnerId(id)) ?: throw ContentPartnerNotFoundException(id)
        return ContentPartner(
            contentPartnerId = contentPartner.contentPartnerId,
            name = contentPartner.name,
            ageRange = AgeRange.bounded(
                contentPartner.ageRange.min(),
                contentPartner.ageRange.max()
            ),
            legalRestrictions = null
        )
    }

    fun getDistributionMethods(contentPartnerId: ContentPartnerId): Set<DistributionMethod>? {
        return find(contentPartnerId)?.distributionMethods
    }

    private fun find(contentPartnerId: ContentPartnerId): com.boclips.contentpartner.service.domain.model.ContentPartner? {
        return contentPartnerRepository.findById(contentPartnerId)
    }
}

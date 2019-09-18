package com.boclips.contentpartner.service.domain.model

import com.boclips.videos.service.domain.model.common.AgeRange
import com.boclips.videos.service.domain.model.legal.restrictions.LegalRestrictions
import com.boclips.videos.service.domain.model.video.DistributionMethod

sealed class ContentPartnerUpdateCommand(val contentPartnerId: ContentPartnerId) {

    class ReplaceName(contentPartnerId: ContentPartnerId, val name: String) :
        ContentPartnerUpdateCommand(contentPartnerId)

    class ReplaceAgeRange(contentPartnerId: ContentPartnerId, val ageRange: AgeRange) :
        ContentPartnerUpdateCommand(contentPartnerId)

    class ReplaceDistributionMethods(
        contentPartnerId: ContentPartnerId, val distributionMethods: Set<DistributionMethod>
    ) : ContentPartnerUpdateCommand(contentPartnerId)

    class ReplaceLegalRestrictions(contentPartnerId: ContentPartnerId, val legalRestrictions: LegalRestrictions) :
        ContentPartnerUpdateCommand(contentPartnerId)
}

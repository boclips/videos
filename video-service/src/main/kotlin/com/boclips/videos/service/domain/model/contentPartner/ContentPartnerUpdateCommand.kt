package com.boclips.videos.service.domain.model.contentPartner

import com.boclips.videos.service.domain.model.common.AgeRange

sealed class ContentPartnerUpdateCommand(val contentPartnerId: ContentPartnerId) {

    class ReplaceName(contentPartnerId: ContentPartnerId, val name: String) :
        ContentPartnerUpdateCommand(contentPartnerId)

    class ReplaceAgeRange(contentPartnerId: ContentPartnerId, val ageRange: AgeRange) :
        ContentPartnerUpdateCommand(contentPartnerId)

    class SetSearchability(contentPartnerId: ContentPartnerId, val searchable: Boolean) :
        ContentPartnerUpdateCommand(contentPartnerId)
}

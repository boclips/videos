package com.boclips.videos.service.domain.model.contentPartner

import com.boclips.videos.service.domain.model.common.AgeRange
import com.boclips.videos.service.domain.model.video.DeliveryMethod

sealed class ContentPartnerUpdateCommand(val contentPartnerId: ContentPartnerId) {

    class ReplaceName(contentPartnerId: ContentPartnerId, val name: String) :
        ContentPartnerUpdateCommand(contentPartnerId)

    class ReplaceAgeRange(contentPartnerId: ContentPartnerId, val ageRange: AgeRange) :
        ContentPartnerUpdateCommand(contentPartnerId)

    class SetSearchability(contentPartnerId: ContentPartnerId, val searchable: Boolean) :
        ContentPartnerUpdateCommand(contentPartnerId)

    class SetHiddenDeliveryMethods(contentPartnerId: ContentPartnerId, val methods: Set<DeliveryMethod>) :
        ContentPartnerUpdateCommand(contentPartnerId)

}

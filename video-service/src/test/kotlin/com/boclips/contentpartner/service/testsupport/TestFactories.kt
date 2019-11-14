package com.boclips.contentpartner.service.testsupport

import com.boclips.contentpartner.service.domain.model.ContentPartner
import com.boclips.contentpartner.service.domain.model.ContentPartnerId
import com.boclips.contentpartner.service.domain.model.Credit
import com.boclips.contentpartner.service.domain.model.LegalRestrictions
import com.boclips.contentpartner.service.domain.model.LegalRestrictionsId
import com.boclips.contentpartner.service.domain.model.Remittance
import com.boclips.contentpartner.service.infrastructure.ContentPartnerDocument
import com.boclips.contentpartner.service.presentation.ContentPartnerRequest
import com.boclips.contentpartner.service.presentation.LegalRestrictionsRequest
import com.boclips.videos.service.domain.model.common.AgeRange
import com.boclips.videos.service.domain.model.video.DistributionMethod
import com.boclips.videos.service.infrastructure.video.DistributionMethodDocument
import com.boclips.videos.service.presentation.ageRange.AgeRangeRequest
import com.boclips.videos.service.presentation.deliveryMethod.DistributionMethodResource
import com.boclips.videos.service.testsupport.TestFactories
import org.bson.types.ObjectId

object TestFactories {

    fun aValidId(): String {
        return ObjectId().toHexString()
    }

    fun createContentPartner(
        id: ContentPartnerId = ContentPartnerId(
            ObjectId().toHexString()
        ),
        name: String = "TED",
        ageRange: AgeRange = AgeRange.bounded(5, 11),
        credit: Credit = Credit.PartnerCredit,
        legalRestrictions: LegalRestrictions? = null,
        distributionMethods: Set<DistributionMethod> = emptySet(),
        remittance: Remittance? = null
    ): ContentPartner {
        return ContentPartner(
            contentPartnerId = id,
            name = name,
            ageRange = ageRange,
            credit = credit,
            legalRestrictions = legalRestrictions,
            distributionMethods = distributionMethods,
            remittance = remittance
        )
    }

    fun createContentPartnerDocument(
        objectId: ObjectId = ObjectId.get(),
        youtubeChannelId: String? = null,
        name: String = "content partner",
        ageRangeMax: Nothing? = null,
        ageRangeMin: Nothing? = null,
        distributionMethods: Set<DistributionMethodDocument>? = null
    ) = ContentPartnerDocument(
        id = objectId,
        youtubeChannelId = youtubeChannelId,
        name = name,
        ageRangeMax = ageRangeMax,
        ageRangeMin = ageRangeMin,
        distributionMethods = distributionMethods
    )

    fun createContentPartnerRequest(
        name: String? = "TED",
        ageRange: AgeRangeRequest? = AgeRangeRequest(
            min = 5,
            max = 11
        ),
        accreditedToYtChannel: String? = null,
        distributionMethods: Set<DistributionMethodResource>? = null,
        legalRestrictions: LegalRestrictionsRequest? = null,
        currency: String? = null
    ): ContentPartnerRequest {
        return ContentPartnerRequest(
            name = name,
            ageRange = ageRange,
            accreditedToYtChannelId = accreditedToYtChannel,
            distributionMethods = distributionMethods,
            legalRestrictions = legalRestrictions,
            currency = currency
        )
    }

    fun createLegalRestrictions(text: String = "No restrictions."): LegalRestrictions {
        return LegalRestrictions(
            id = LegalRestrictionsId(TestFactories.aValidId()),
            text = text
        )
    }
}

package com.boclips.contentpartner.service.testsupport

import com.boclips.contentpartner.service.domain.model.AgeRange
import com.boclips.contentpartner.service.domain.model.ContentCategory
import com.boclips.contentpartner.service.domain.model.ContentPartner
import com.boclips.contentpartner.service.domain.model.ContentPartnerId
import com.boclips.contentpartner.service.domain.model.Credit
import com.boclips.contentpartner.service.domain.model.DistributionMethod
import com.boclips.contentpartner.service.domain.model.LegalRestriction
import com.boclips.contentpartner.service.domain.model.LegalRestrictionsId
import com.boclips.contentpartner.service.domain.model.Remittance
import com.boclips.contentpartner.service.domain.model.RequestContext
import com.boclips.contentpartner.service.domain.model.User
import com.boclips.contentpartner.service.domain.model.UserId
import com.boclips.contentpartner.service.infrastructure.ContentCategoryDocument
import com.boclips.contentpartner.service.infrastructure.ContentPartnerDocument
import com.boclips.videos.service.infrastructure.video.DistributionMethodDocument
import com.boclips.videos.service.testsupport.TestFactories
import org.bson.types.ObjectId
import java.util.Locale

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
        legalRestriction: LegalRestriction? = null,
        distributionMethods: Set<DistributionMethod> = emptySet(),
        remittance: Remittance? = null,
        description: String? = null,
        contentCategories: List<ContentCategory>? = emptyList(),
        hubspotId: String? = null,
        awards: String? = null,
        notes: String? = null,
        language: Locale? = null
    ): ContentPartner {
        return ContentPartner(
            contentPartnerId = id,
            name = name,
            ageRange = ageRange,
            credit = credit,
            legalRestriction = legalRestriction,
            distributionMethods = distributionMethods,
            remittance = remittance,
            description = description,
            contentCategories = contentCategories,
            hubspotId = hubspotId,
            awards = awards,
            notes = notes,
            language = language
        )
    }

    fun createContentPartnerDocument(
        objectId: ObjectId = ObjectId.get(),
        youtubeChannelId: String? = null,
        name: String = "content partner",
        ageRangeMax: Nothing? = null,
        ageRangeMin: Nothing? = null,
        distributionMethods: Set<DistributionMethodDocument>? = null,
        contentCategories: List<ContentCategoryDocument>? = emptyList(),
        hubspotId: String? = null,
        awards: String? = null,
        notes: String? = null,
        language: String? = null
    ) = ContentPartnerDocument(
        id = objectId,
        youtubeChannelId = youtubeChannelId,
        name = name,
        ageRangeMax = ageRangeMax,
        ageRangeMin = ageRangeMin,
        distributionMethods = distributionMethods,
        contentCategories = contentCategories,
        hubspotId = hubspotId,
        awards = awards,
        notes = notes,
        language = language
    )

    fun createLegalRestrictions(text: String = "No restrictions."): LegalRestriction {
        return LegalRestriction(
            id = LegalRestrictionsId(TestFactories.aValidId()),
            text = text
        )
    }
}

object UserFactory {
    fun sample(
        isAdministrator: Boolean = false,
        id: String = "userio-123"
    ): User {
        return User(
            id = UserId(id),
            isPermittedToAccessBackoffice = isAdministrator,
            context = RequestContext(origin = "https://teachers.boclips.com")
        )
    }
}

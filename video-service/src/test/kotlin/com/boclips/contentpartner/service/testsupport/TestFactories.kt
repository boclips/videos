package com.boclips.contentpartner.service.testsupport

import com.boclips.contentpartner.service.domain.model.AgeRange
import com.boclips.contentpartner.service.domain.model.ContentPartner
import com.boclips.contentpartner.service.domain.model.ContentPartnerId
import com.boclips.contentpartner.service.domain.model.ContentPartnerType
import com.boclips.contentpartner.service.domain.model.Credit
import com.boclips.contentpartner.service.domain.model.DistributionMethod
import com.boclips.contentpartner.service.domain.model.LegalRestriction
import com.boclips.contentpartner.service.domain.model.LegalRestrictionsId
import com.boclips.contentpartner.service.domain.model.EduAgeRange
import com.boclips.contentpartner.service.domain.model.EduAgeRangeId
import com.boclips.contentpartner.service.domain.model.Remittance
import com.boclips.contentpartner.service.domain.model.RequestContext
import com.boclips.contentpartner.service.domain.model.User
import com.boclips.contentpartner.service.domain.model.UserId
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
        contentCategories: List<String>? = emptyList(),
        hubspotId: String? = null,
        awards: String? = null,
        notes: String? = null,
        language: Locale? = null,
        contentTypes: List<ContentPartnerType>? = emptyList()
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
            language = language,
            contentTypes = contentTypes
        )
    }

    fun createContentPartnerDocument(
        objectId: ObjectId = ObjectId.get(),
        youtubeChannelId: String? = null,
        name: String = "content partner",
        ageRangeMax: Nothing? = null,
        ageRangeMin: Nothing? = null,
        distributionMethods: Set<DistributionMethodDocument>? = null,
        contentCategories: List<String>? = emptyList(),
        hubspotId: String? = null,
        awards: String? = null,
        notes: String? = null,
        language: String? = null,
        contentTypes: List<String>? = emptyList()
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
        language = language,
        contentTypes = contentTypes
    )

    fun createLegalRestrictions(text: String = "No restrictions."): LegalRestriction {
        return LegalRestriction(
            id = LegalRestrictionsId(TestFactories.aValidId()),
            text = text
        )
    }

    fun createEduAgeRange(id: String = "123", label: String = "Label", min: Int = 3, max: Int = 5): EduAgeRange {
        return EduAgeRange(
            EduAgeRangeId(id), label, min, max
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

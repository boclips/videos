package com.boclips.contentpartner.service.testsupport

import com.boclips.contentpartner.service.domain.model.agerange.AgeRange
import com.boclips.contentpartner.service.domain.model.agerange.AgeRangeBuckets
import com.boclips.contentpartner.service.domain.model.agerange.AgeRangeId
import com.boclips.contentpartner.service.domain.model.contentpartner.ContentPartner
import com.boclips.contentpartner.service.domain.model.contentpartner.ContentPartnerId
import com.boclips.contentpartner.service.domain.model.contentpartner.ContentPartnerMarketingInformation
import com.boclips.contentpartner.service.domain.model.contentpartner.ContentPartnerType
import com.boclips.contentpartner.service.domain.model.contentpartner.Credit
import com.boclips.contentpartner.service.domain.model.contentpartner.DistributionMethod
import com.boclips.contentpartner.service.domain.model.contentpartner.IngestDetails
import com.boclips.contentpartner.service.domain.model.contentpartner.ManualIngest
import com.boclips.contentpartner.service.domain.model.contentpartner.PedagogyInformation
import com.boclips.contentpartner.service.domain.model.contentpartner.Remittance
import com.boclips.contentpartner.service.domain.model.contentpartnercontract.ContentPartnerContract
import com.boclips.contentpartner.service.domain.model.legalrestriction.LegalRestriction
import com.boclips.contentpartner.service.domain.model.legalrestriction.LegalRestrictionsId
import com.boclips.contentpartner.service.infrastructure.agerange.AgeRangeDocument
import com.boclips.contentpartner.service.infrastructure.contentpartner.ContentPartnerDocument
import com.boclips.contentpartner.service.infrastructure.contentpartner.IngestDetailsDocument
import com.boclips.contentpartner.service.infrastructure.contract.ContentPartnerContractDocument
import com.boclips.videos.api.common.IngestType
import com.boclips.videos.api.response.contentpartner.IngestDetailsResource
import com.boclips.videos.service.infrastructure.video.DistributionMethodDocument
import com.boclips.videos.service.testsupport.TestFactories
import org.bson.types.ObjectId
import java.time.Period
import java.util.Locale

object ContentPartnerFactory {

    fun aValidId(): String {
        return ObjectId().toHexString()
    }

    fun createContentPartner(
        id: ContentPartnerId = ContentPartnerId(
            ObjectId().toHexString()
        ),
        name: String = "TED",
        ageRanges: AgeRangeBuckets = AgeRangeBuckets(
            emptyList()
        ),
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
        contentTypes: List<ContentPartnerType>? = emptyList(),
        ingest: IngestDetails = ManualIngest,
        deliveryFrequency: Period? = null,
        pedagogyInformation: PedagogyInformation? = null,
        marketingInformation: ContentPartnerMarketingInformation? = null,
        contract: ContentPartnerContract? = null
    ): ContentPartner {
        return ContentPartner(
            contentPartnerId = id,
            name = name,
            ageRangeBuckets = ageRanges,
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
            contentTypes = contentTypes,
            ingest = ingest,
            deliveryFrequency = deliveryFrequency,
            marketingInformation = marketingInformation,
            pedagogyInformation = pedagogyInformation,
            contract = contract
        )
    }

    fun createContentPartnerDocument(
        objectId: ObjectId = ObjectId.get(),
        youtubeChannelId: String? = null,
        name: String = "content partner",
        distributionMethods: Set<DistributionMethodDocument>? = null,
        contentCategories: List<String>? = emptyList(),
        hubspotId: String? = null,
        awards: String? = null,
        notes: String? = null,
        language: String? = null,
        contentTypes: List<String>? = emptyList(),
        ingest: IngestDetailsDocument? = null,
        isTranscriptProvided: Boolean? = null,
        ageRanges: List<AgeRangeDocument>? = emptyList(),
        contract: ContentPartnerContractDocument? = null
    ) = ContentPartnerDocument(
        id = objectId,
        youtubeChannelId = youtubeChannelId,
        name = name,
        distributionMethods = distributionMethods,
        contentCategories = contentCategories,
        hubspotId = hubspotId,
        awards = awards,
        notes = notes,
        language = language,
        contentTypes = contentTypes,
        ingest = ingest,
        ageRanges = ageRanges,
        isTranscriptProvided = isTranscriptProvided,
        contract = contract
    )

    fun createIngestDetailsResource(
        type: IngestType = IngestType.MANUAL,
        playlistIds: List<String>? = null,
        urls: List<String>? = null
    ): IngestDetailsResource {
        return IngestDetailsResource(
            type = type,
            playlistIds = playlistIds,
            urls = urls
        )
    }

    fun createLegalRestrictions(text: String = "No restrictions."): LegalRestriction {
        return LegalRestriction(
            id = LegalRestrictionsId(
                TestFactories.aValidId()
            ),
            text = text
        )
    }

    fun createAgeRange(id: String = "123", label: String = "Label", min: Int = 3, max: Int? = 5): AgeRange {
        return AgeRange(
            AgeRangeId(id), label, min, max
        )
    }
}


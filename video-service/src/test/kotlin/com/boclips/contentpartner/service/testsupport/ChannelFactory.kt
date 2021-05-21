package com.boclips.contentpartner.service.testsupport

import com.boclips.contentpartner.service.domain.model.agerange.AgeRange
import com.boclips.contentpartner.service.domain.model.agerange.AgeRangeId
import com.boclips.contentpartner.service.domain.model.channel.*
import com.boclips.contentpartner.service.domain.model.contract.Contract
import com.boclips.contentpartner.service.domain.model.legalrestriction.LegalRestriction
import com.boclips.contentpartner.service.domain.model.legalrestriction.LegalRestrictionsId
import com.boclips.contentpartner.service.infrastructure.agerange.AgeRangeDocument
import com.boclips.contentpartner.service.infrastructure.channel.ChannelDocument
import com.boclips.contentpartner.service.infrastructure.channel.IngestDetailsDocument
import com.boclips.contentpartner.service.infrastructure.channel.TaxonomyDocument
import com.boclips.contentpartner.service.infrastructure.contract.ContractDocument
import com.boclips.search.service.domain.channels.model.CategoryCode
import com.boclips.videos.api.common.IngestType
import com.boclips.videos.api.response.channel.IngestDetailsResource
import com.boclips.videos.service.domain.model.suggestions.ChannelSuggestion
import com.boclips.videos.service.infrastructure.video.DistributionMethodDocument
import com.boclips.videos.service.testsupport.TestFactories
import org.bson.types.ObjectId
import java.time.Period
import java.util.*

object ChannelFactory {

    fun aValidId(): String {
        return ObjectId().toHexString()
    }

    fun createChannel(
        id: ChannelId = ChannelId(
            ObjectId().toHexString()
        ),
        name: String = "TED",
        legalRestriction: LegalRestriction? = null,
        distributionMethods: Set<DistributionMethod> = emptySet(),
        remittance: Remittance? = null,
        description: String? = null,
        contentCategories: List<ContentCategory>? = emptyList(),
        hubspotId: String? = null,
        awards: String? = null,
        notes: String? = null,
        language: Locale? = null,
        contentTypes: List<ContentType>? = emptyList(),
        ingest: IngestDetails = ManualIngest,
        pedagogyInformation: PedagogyInformation? = null,
        marketingInformation: MarketingInformation? = null,
        contract: Contract? = null,
        taxonomy: Taxonomy? = null
    ): Channel {
        return Channel(
            id = id,
            name = name,
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
            marketingInformation = marketingInformation,
            pedagogyInformation = pedagogyInformation,
            contract = contract,
            taxonomy = taxonomy
        )
    }

    fun createChannelSuggestion(
        id: ChannelId = ChannelId(
            ObjectId().toHexString()
        ),
        name: String = "TED",
        eligibleForStream: Boolean = false,
        contentTypes: List<ContentType> = emptyList(),
        taxonomy: Taxonomy = Taxonomy.VideoLevelTagging,
    ): ChannelSuggestion {
        return ChannelSuggestion(
            id = id,
            name = name,
            eligibleForStream = eligibleForStream,
            contentTypes = contentTypes,
            taxonomy = taxonomy
        )
    }

    fun createChannelDocument(
        objectId: ObjectId = ObjectId.get(),
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
        contract: ContractDocument? = null,
        taxonomy: TaxonomyDocument? = null
    ) = ChannelDocument(
        id = objectId,
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
        contract = contract,
        taxonomy = taxonomy
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

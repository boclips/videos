package com.boclips.contentpartner.service.application.channel

import com.boclips.contentpartner.service.application.exceptions.*
import com.boclips.contentpartner.service.domain.model.agerange.AgeRangeBuckets
import com.boclips.contentpartner.service.domain.model.agerange.AgeRangeId
import com.boclips.contentpartner.service.domain.model.agerange.AgeRangeRepository
import com.boclips.contentpartner.service.domain.model.channel.*
import com.boclips.contentpartner.service.domain.model.contract.ContractId
import com.boclips.contentpartner.service.domain.model.contract.ContractRepository
import com.boclips.contentpartner.service.domain.service.channel.ChannelService
import com.boclips.contentpartner.service.presentation.converters.ContentPartnerMarketingInformationConverter
import com.boclips.contentpartner.service.presentation.converters.DistributionMethodResourceConverter
import com.boclips.contentpartner.service.presentation.converters.IngestDetailsResourceConverter
import com.boclips.videos.api.request.channel.ChannelRequest
import com.boclips.videos.service.application.GetCategoryWithAncestors
import org.bson.types.ObjectId
import java.util.*

class CreateChannel(
    private val channelService: ChannelService,
    private val ageRangeRepository: AgeRangeRepository,
    private val ingestDetailsToResourceConverter: IngestDetailsResourceConverter,
    private val contractRepository: ContractRepository,
    private val getCategoryWithAncestors: GetCategoryWithAncestors
) {
    operator fun invoke(upsertRequest: ChannelRequest): Channel {
        val ageRanges = upsertRequest.ageRanges.orEmpty().map { rawAgeRangeId ->
            AgeRangeId(rawAgeRangeId).let { ageRangeId ->
                ageRangeRepository.findById(ageRangeId) ?: throw InvalidAgeRangeException(ageRangeId)
            }
        }

        val methods = upsertRequest.distributionMethods?.let(
            DistributionMethodResourceConverter::toDistributionMethods
        ) ?: DistributionMethod.ALL

        val contract = upsertRequest.contractId?.let {
            val contractId = ContractId(it)

            contractRepository.findById(contractId)
                ?: throw InvalidContractException(contractId)
        }

        val channel = Channel(
            id = ChannelId(
                value = ObjectId().toHexString()
            ),
            name = upsertRequest.name!!,
            legalRestriction = null,
            distributionMethods = methods,
            remittance = upsertRequest.currency?.let {
                Remittance(
                    Currency.getInstance(it)
                )
            },
            description = upsertRequest.description,
            contentCategories = upsertRequest.contentCategories?.let { ContentCategoryConverter.convert(it) },
            notes = upsertRequest.notes,
            language = upsertRequest.language?.let(Locale::forLanguageTag),
            contentTypes = upsertRequest.contentTypes?.mapNotNull {
                when (it) {
                    "NEWS" -> ContentType.NEWS
                    "INSTRUCTIONAL" -> ContentType.INSTRUCTIONAL
                    "STOCK" -> ContentType.STOCK
                    else -> null
                }
            },
            ingest = upsertRequest.ingest?.let { ingestDetailsToResourceConverter.fromResource(it) }
                ?: ManualIngest,
            pedagogyInformation = PedagogyInformation(
                bestForTags = upsertRequest.bestForTags,
                subjects = upsertRequest.subjects,
                ageRangeBuckets = AgeRangeBuckets(
                    ageRanges
                )
            ),
            marketingInformation = ContentPartnerMarketingInformationConverter.convert(upsertRequest),
            contract = contract,
            taxonomy = if (upsertRequest.requiresVideoLevelTagging == true) {
                Taxonomy.VideoLevelTagging
            } else {
                Taxonomy.ChannelLevelTagging(
                    categories = upsertRequest.categories?.map { categoryCode ->
                        getCategoryWithAncestors(categoryCode)
                    }?.toSet() ?: emptySet()
                )
            },
        )

        return when (val createdChannelResult = channelService.create(channel)) {
            is CreateChannelResult.Success -> createdChannelResult.channel
            is CreateChannelResult.NameConflict -> throw ChannelConflictException(createdChannelResult.name)
            CreateChannelResult.MissingContract -> throw MissingContractException()
        }
    }
}

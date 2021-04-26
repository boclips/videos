package com.boclips.contentpartner.service.application.channel

import com.boclips.contentpartner.service.application.exceptions.ChannelConflictException
import com.boclips.contentpartner.service.application.exceptions.ChannelHubspotIdException
import com.boclips.contentpartner.service.application.exceptions.InvalidAgeRangeException
import com.boclips.contentpartner.service.application.exceptions.InvalidContractException
import com.boclips.contentpartner.service.application.exceptions.MissingContractException
import com.boclips.contentpartner.service.domain.model.agerange.AgeRangeBuckets
import com.boclips.contentpartner.service.domain.model.agerange.AgeRangeId
import com.boclips.contentpartner.service.domain.model.agerange.AgeRangeRepository
import com.boclips.contentpartner.service.domain.model.channel.Channel
import com.boclips.contentpartner.service.domain.model.channel.ChannelId
import com.boclips.contentpartner.service.domain.model.channel.ContentType
import com.boclips.contentpartner.service.domain.model.channel.CreateChannelResult
import com.boclips.contentpartner.service.domain.model.channel.DistributionMethod
import com.boclips.contentpartner.service.domain.model.channel.ManualIngest
import com.boclips.contentpartner.service.domain.model.channel.PedagogyInformation
import com.boclips.contentpartner.service.domain.model.channel.Remittance
import com.boclips.contentpartner.service.domain.model.contract.ContractId
import com.boclips.contentpartner.service.domain.model.contract.ContractRepository
import com.boclips.contentpartner.service.domain.service.channel.ChannelService
import com.boclips.contentpartner.service.presentation.converters.ContentPartnerMarketingInformationConverter
import com.boclips.contentpartner.service.presentation.converters.DistributionMethodResourceConverter
import com.boclips.contentpartner.service.presentation.converters.IngestDetailsResourceConverter
import com.boclips.videos.api.request.channel.ChannelRequest
import org.bson.types.ObjectId
import java.util.Currency
import java.util.Locale

class CreateChannel(
    private val channelService: ChannelService,
    private val ageRangeRepository: AgeRangeRepository,
    private val ingestDetailsToResourceConverter: IngestDetailsResourceConverter,
    private val contractRepository: ContractRepository
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
            hubspotId = upsertRequest.hubspotId,
            awards = upsertRequest.awards,
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
            deliveryFrequency = upsertRequest.deliveryFrequency,
            pedagogyInformation = PedagogyInformation(
                bestForTags = upsertRequest.bestForTags,
                subjects = upsertRequest.subjects,
                ageRangeBuckets = AgeRangeBuckets(
                    ageRanges
                )
            ),
            marketingInformation = ContentPartnerMarketingInformationConverter.convert(upsertRequest),
            contract = contract,
            videoLevelTagging = upsertRequest.videoLevelTagging
        )

        val createdChannelResult = channelService.create(channel)

        return when (createdChannelResult) {
            is CreateChannelResult.Success -> createdChannelResult.channel
            is CreateChannelResult.NameConflict -> throw ChannelConflictException(createdChannelResult.name)
            is CreateChannelResult.HubSpotIdConflict -> throw ChannelHubspotIdException(createdChannelResult.hubSpotId)
            CreateChannelResult.MissingContract -> throw MissingContractException()
        }
    }
}

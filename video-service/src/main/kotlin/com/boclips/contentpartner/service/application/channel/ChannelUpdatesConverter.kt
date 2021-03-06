package com.boclips.contentpartner.service.application.channel

import com.boclips.contentpartner.service.application.exceptions.InvalidContractException
import com.boclips.contentpartner.service.domain.model.agerange.AgeRangeBuckets
import com.boclips.contentpartner.service.domain.model.agerange.AgeRangeId
import com.boclips.contentpartner.service.domain.model.agerange.AgeRangeRepository
import com.boclips.contentpartner.service.domain.model.channel.ChannelId
import com.boclips.contentpartner.service.domain.model.channel.ChannelUpdateCommand
import com.boclips.contentpartner.service.domain.model.channel.ChannelUpdateCommand.ReplaceDistributionMethods
import com.boclips.contentpartner.service.domain.model.contract.ContractId
import com.boclips.contentpartner.service.domain.model.contract.ContractRepository
import com.boclips.contentpartner.service.domain.model.legalrestriction.LegalRestrictionsId
import com.boclips.contentpartner.service.domain.model.legalrestriction.LegalRestrictionsRepository
import com.boclips.contentpartner.service.presentation.converters.ContentPartnerMarketingStatusConverter
import com.boclips.contentpartner.service.presentation.converters.DistributionMethodResourceConverter
import com.boclips.contentpartner.service.presentation.converters.IngestDetailsResourceConverter
import com.boclips.contentpartner.service.presentation.converters.UrlConverter
import com.boclips.videos.api.common.ExplicitlyNull
import com.boclips.videos.api.common.Specified
import com.boclips.videos.api.request.channel.ChannelRequest
import com.boclips.videos.service.application.GetCategoryWithAncestors
import java.util.*

class ChannelUpdatesConverter(
    private val legalRestrictionsRepository: LegalRestrictionsRepository,
    private val ageRangeRepository: AgeRangeRepository,
    private val ingestDetailsResourceConverter: IngestDetailsResourceConverter,
    private val contractRepository: ContractRepository,
    private val getCategoryWithAncestors: GetCategoryWithAncestors,
) {
    fun convert(
        id: ChannelId,
        upsertChannelRequest: ChannelRequest
    ): List<ChannelUpdateCommand> =
        ChannelUpdateCommandCreator(
            id,
            upsertChannelRequest,
            ingestDetailsResourceConverter
        ).let { commandCreator ->
            listOfNotNull(
                commandCreator.updateName(),
                commandCreator.updateAgeRanges(ageRangeRepository),
                commandCreator.updateLegalRestrictions(legalRestrictionsRepository),
                commandCreator.updateHiddenDeliveryMethods(),
                commandCreator.updateCurrency(),
                commandCreator.updateContentPartnerTypes(),
                commandCreator.updateContentContentCategories(),
                commandCreator.updateContentLanguage(),
                commandCreator.updateDescription(),
                commandCreator.updateNotes(),
                commandCreator.updateOneLineDescription(),
                commandCreator.updateMarketingStatus(),
                commandCreator.updateMarketingLogos(),
                commandCreator.updateMarketingShowreel(),
                commandCreator.updateMarketingSampleVideos(),
                commandCreator.updateBestForTags(),
                commandCreator.updateSubjects(),
                commandCreator.updateIngestDetails(),
                commandCreator.updateContract(contractRepository),
                commandCreator.updateCategories(getCategoryWithAncestors),
                commandCreator.updateRequiresVideoLevelTagging(),
            )
        }
}

class ChannelUpdateCommandCreator(
    val id: ChannelId,
    private val channelRequest: ChannelRequest,
    private val ingestDetailsResourceConverter: IngestDetailsResourceConverter
) {

    fun updateHiddenDeliveryMethods(): ChannelUpdateCommand? {
        return channelRequest.distributionMethods
            ?.let {
                DistributionMethodResourceConverter.toDistributionMethods(it)
            }
            ?.let { deliveryMethods ->
                ReplaceDistributionMethods(channelId = id, distributionMethods = deliveryMethods)
            }
    }

    fun updateName(): ChannelUpdateCommand.ReplaceName? =
        channelRequest.name?.let {
            ChannelUpdateCommand.ReplaceName(channelId = id, name = it)
        }

    fun updateAgeRanges(ageRangeRepository: AgeRangeRepository): ChannelUpdateCommand.ReplaceAgeRanges? =
        channelRequest.ageRanges?.let {
            val ageRanges = it.mapNotNull { ageRangeId ->
                ageRangeRepository.findById(
                    AgeRangeId(
                        ageRangeId
                    )
                )
            }
            ChannelUpdateCommand.ReplaceAgeRanges(
                id,
                AgeRangeBuckets(ageRanges = ageRanges)
            )
        }

    fun updateLegalRestrictions(legalRestrictionsRepository: LegalRestrictionsRepository): ChannelUpdateCommand.ReplaceLegalRestrictions? =
        channelRequest.legalRestrictions
            ?.let { restrictionsRequest ->
                legalRestrictionsRepository.findById(
                    LegalRestrictionsId(
                        restrictionsRequest.id!!
                    )
                )
            }
            ?.let { restrictions -> ChannelUpdateCommand.ReplaceLegalRestrictions(id, restrictions) }

    fun updateCurrency(): ChannelUpdateCommand.ReplaceCurrency? =
        channelRequest.currency?.let {
            ChannelUpdateCommand.ReplaceCurrency(id, Currency.getInstance(it))
        }

    fun updateContentPartnerTypes(): ChannelUpdateCommand.ReplaceContentTypes? =
        channelRequest.contentTypes?.let { contentTypes ->
            ChannelUpdateCommand.ReplaceContentTypes(id, contentTypes.mapNotNull { it })
        }

    fun updateContentContentCategories(): ChannelUpdateCommand.ReplaceContentCategories? =
        channelRequest.contentCategories?.let { contentCategories ->
            ChannelUpdateCommand.ReplaceContentCategories(id, ContentCategoryConverter.convert(contentCategories))
        }

    fun updateContentLanguage(): ChannelUpdateCommand.ReplaceLanguage? =
        channelRequest.language?.let { language ->
            ChannelUpdateCommand.ReplaceLanguage(id, language)
        }

    fun updateDescription(): ChannelUpdateCommand.ReplaceDescription? =
        channelRequest.description?.let { description ->
            ChannelUpdateCommand.ReplaceDescription(id, description)
        }

    fun updateNotes(): ChannelUpdateCommand.ReplaceNotes? =
        channelRequest.notes?.let { notes ->
            ChannelUpdateCommand.ReplaceNotes(id, notes)
        }

    fun updateOneLineDescription(): ChannelUpdateCommand.ReplaceOneLineDescription? =
        channelRequest.oneLineDescription?.let {
            ChannelUpdateCommand.ReplaceOneLineDescription(id, it)
        }

    fun updateMarketingStatus(): ChannelUpdateCommand.ReplaceMarketingStatus? =
        channelRequest.marketingInformation?.status?.let {
            ChannelUpdateCommand.ReplaceMarketingStatus(id, ContentPartnerMarketingStatusConverter.convert(it))
        }

    fun updateMarketingLogos(): ChannelUpdateCommand.ReplaceMarketingLogos? =
        channelRequest.marketingInformation?.logos?.let {
            ChannelUpdateCommand.ReplaceMarketingLogos(id, it.map(UrlConverter::convert))
        }

    fun updateMarketingShowreel(): ChannelUpdateCommand.ReplaceMarketingShowreel? =
        channelRequest.marketingInformation?.showreel?.let {
            ChannelUpdateCommand.ReplaceMarketingShowreel(
                id,
                when (it) {
                    is Specified -> UrlConverter.convert(it.value)
                    is ExplicitlyNull -> null
                }
            )
        }

    fun updateMarketingSampleVideos(): ChannelUpdateCommand.ReplaceMarketingSampleVideos? =
        channelRequest.marketingInformation?.sampleVideos?.let {
            ChannelUpdateCommand.ReplaceMarketingSampleVideos(id, it.map(UrlConverter::convert))
        }

    fun updateBestForTags(): ChannelUpdateCommand.ReplaceBestForTags? =
        channelRequest.bestForTags?.let {
            ChannelUpdateCommand.ReplaceBestForTags(id, it)
        }

    fun updateSubjects(): ChannelUpdateCommand.ReplaceSubjects? =
        channelRequest.subjects?.let {
            ChannelUpdateCommand.ReplaceSubjects(id, it)
        }

    fun updateIngestDetails(): ChannelUpdateCommand.ReplaceIngestDetails? =
        channelRequest.ingest?.let {
            ChannelUpdateCommand.ReplaceIngestDetails(id, ingestDetailsResourceConverter.fromResource(it))
        }

    fun updateContract(contractRepository: ContractRepository): ChannelUpdateCommand.ReplaceContract? =
        channelRequest.contractId?.let {
            val contractId = ContractId(it)

            val contract = contractRepository.findById(contractId)
                ?: throw InvalidContractException(contractId)

            ChannelUpdateCommand.ReplaceContract(id, contract)
        }

    fun updateCategories(getCategoryWithAncestors: GetCategoryWithAncestors): ChannelUpdateCommand.ReplaceCategories? {
        if (channelRequest.requiresVideoLevelTagging == true) {
            return ChannelUpdateCommand.ReplaceCategories(id, emptySet())
        }

        val categories = channelRequest.categories?.map { categoryCode ->
            getCategoryWithAncestors(categoryCode)
        }?.toSet()

        return categories?.let { ChannelUpdateCommand.ReplaceCategories(id, it) }
    }

    fun updateRequiresVideoLevelTagging(): ChannelUpdateCommand.ReplaceRequiresVideoLevelTagging? =
        channelRequest.requiresVideoLevelTagging?.let {
            ChannelUpdateCommand.ReplaceRequiresVideoLevelTagging(id, it)
        }
}

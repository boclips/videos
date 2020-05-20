package com.boclips.contentpartner.service.application.channel

import com.boclips.contentpartner.service.application.exceptions.InvalidContractException
import com.boclips.contentpartner.service.domain.model.agerange.AgeRangeBuckets
import com.boclips.contentpartner.service.domain.model.agerange.AgeRangeId
import com.boclips.contentpartner.service.domain.model.agerange.AgeRangeRepository
import com.boclips.contentpartner.service.domain.model.channel.ChannelId
import com.boclips.contentpartner.service.domain.model.channel.ChannelUpdateCommand
import com.boclips.contentpartner.service.domain.model.channel.ChannelUpdateCommand.ReplaceDistributionMethods
import com.boclips.contentpartner.service.domain.model.contentpartnercontract.ContentPartnerContractId
import com.boclips.contentpartner.service.domain.model.contentpartnercontract.ContentPartnerContractRepository
import com.boclips.contentpartner.service.domain.model.legalrestriction.LegalRestrictionsId
import com.boclips.contentpartner.service.domain.model.legalrestriction.LegalRestrictionsRepository
import com.boclips.contentpartner.service.presentation.converters.ContentPartnerMarketingStatusConverter
import com.boclips.contentpartner.service.presentation.converters.DistributionMethodResourceConverter
import com.boclips.contentpartner.service.presentation.converters.IngestDetailsResourceConverter
import com.boclips.contentpartner.service.presentation.converters.UrlConverter
import com.boclips.videos.api.common.ExplicitlyNull
import com.boclips.videos.api.common.Specified
import com.boclips.videos.api.request.contentpartner.ContentPartnerRequest
import java.util.Currency

class ChannelUpdatesConverter(
    private val legalRestrictionsRepository: LegalRestrictionsRepository,
    private val ageRangeRepository: AgeRangeRepository,
    private val ingestDetailsResourceConverter: IngestDetailsResourceConverter,
    private val contentPartnerContractRepository: ContentPartnerContractRepository
) {
    fun convert(
        id: ChannelId,
        upsertContentPartnerRequest: ContentPartnerRequest
    ): List<ChannelUpdateCommand> =
        ChannelUpdateCommandCreator(
            id,
            upsertContentPartnerRequest,
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
                commandCreator.updateAwards(),
                commandCreator.updateHubspotId(),
                commandCreator.updateNotes(),
                commandCreator.updateOneLineDescription(),
                commandCreator.updateMarketingStatus(),
                commandCreator.updateMarketingLogos(),
                commandCreator.updateMarketingShowreel(),
                commandCreator.updateMarketingSampleVideos(),
                commandCreator.updateIsTranscriptProvided(),
                commandCreator.updateEducationalResources(),
                commandCreator.updateCurriculumAligned(),
                commandCreator.updateBestForTags(),
                commandCreator.updateSubjects(),
                commandCreator.updateIngestDetails(),
                commandCreator.updateDeliveryFrequency(),
                commandCreator.updateContract(contentPartnerContractRepository)
            )
        }
}

class ChannelUpdateCommandCreator(
    val id: ChannelId,
    private val contentPartnerRequest: ContentPartnerRequest,
    private val ingestDetailsResourceConverter: IngestDetailsResourceConverter
) {

    fun updateHiddenDeliveryMethods(): ChannelUpdateCommand? {
        return contentPartnerRequest.distributionMethods
            ?.let {
                DistributionMethodResourceConverter.toDistributionMethods(it)
            }
            ?.let { deliveryMethods ->
                ReplaceDistributionMethods(channelId = id, distributionMethods = deliveryMethods)
            }
    }

    fun updateName(): ChannelUpdateCommand.ReplaceName? =
        contentPartnerRequest.name?.let {
            ChannelUpdateCommand.ReplaceName(channelId = id, name = it)
        }

    fun updateAgeRanges(ageRangeRepository: AgeRangeRepository): ChannelUpdateCommand.ReplaceAgeRanges? =
        contentPartnerRequest.ageRanges?.let {
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
        contentPartnerRequest.legalRestrictions
            ?.let { restrictionsRequest ->
                legalRestrictionsRepository.findById(
                    LegalRestrictionsId(
                        restrictionsRequest.id!!
                    )
                )
            }
            ?.let { restrictions -> ChannelUpdateCommand.ReplaceLegalRestrictions(id, restrictions) }

    fun updateCurrency(): ChannelUpdateCommand.ReplaceCurrency? =
        contentPartnerRequest.currency?.let {
            ChannelUpdateCommand.ReplaceCurrency(id, Currency.getInstance(it))
        }

    fun updateContentPartnerTypes(): ChannelUpdateCommand.ReplaceContentTypes? =
        contentPartnerRequest.contentTypes?.let { contentTypes ->
            ChannelUpdateCommand.ReplaceContentTypes(id, contentTypes.mapNotNull { it })
        }

    fun updateContentContentCategories(): ChannelUpdateCommand.ReplaceContentCategories? =
        contentPartnerRequest.contentCategories?.let { contentCategories ->
            ChannelUpdateCommand.ReplaceContentCategories(id, contentCategories)
        }

    fun updateContentLanguage(): ChannelUpdateCommand.ReplaceLanguage? =
        contentPartnerRequest.language?.let { language ->
            ChannelUpdateCommand.ReplaceLanguage(id, language)
        }

    fun updateDescription(): ChannelUpdateCommand.ReplaceDescription? =
        contentPartnerRequest.description?.let { description ->
            ChannelUpdateCommand.ReplaceDescription(id, description)
        }

    fun updateAwards(): ChannelUpdateCommand.ReplaceAwards? =
        contentPartnerRequest.awards?.let { awards ->
            ChannelUpdateCommand.ReplaceAwards(id, awards)
        }

    fun updateHubspotId(): ChannelUpdateCommand.ReplaceHubspotId? =
        contentPartnerRequest.hubspotId?.let { hubspotId ->
            ChannelUpdateCommand.ReplaceHubspotId(id, hubspotId)
        }

    fun updateNotes(): ChannelUpdateCommand.ReplaceNotes? =
        contentPartnerRequest.notes?.let { notes ->
            ChannelUpdateCommand.ReplaceNotes(id, notes)
        }

    fun updateOneLineDescription(): ChannelUpdateCommand.ReplaceOneLineDescription? =
        contentPartnerRequest.oneLineDescription?.let {
            ChannelUpdateCommand.ReplaceOneLineDescription(id, it)
        }

    fun updateMarketingStatus(): ChannelUpdateCommand.ReplaceMarketingStatus? =
        contentPartnerRequest.marketingInformation?.status?.let {
            ChannelUpdateCommand.ReplaceMarketingStatus(id, ContentPartnerMarketingStatusConverter.convert(it))
        }

    fun updateMarketingLogos(): ChannelUpdateCommand.ReplaceMarketingLogos? =
        contentPartnerRequest.marketingInformation?.logos?.let {
            ChannelUpdateCommand.ReplaceMarketingLogos(id, it.map(UrlConverter::convert))
        }

    fun updateMarketingShowreel(): ChannelUpdateCommand.ReplaceMarketingShowreel? =
        contentPartnerRequest.marketingInformation?.showreel?.let {
            ChannelUpdateCommand.ReplaceMarketingShowreel(
                id,
                when (it) {
                    is Specified -> UrlConverter.convert(it.value)
                    is ExplicitlyNull -> null
                }
            )
        }

    fun updateMarketingSampleVideos(): ChannelUpdateCommand.ReplaceMarketingSampleVideos? =
        contentPartnerRequest.marketingInformation?.sampleVideos?.let {
            ChannelUpdateCommand.ReplaceMarketingSampleVideos(id, it.map(UrlConverter::convert))
        }

    fun updateIsTranscriptProvided(): ChannelUpdateCommand.ReplaceIsTranscriptProvided? =
        contentPartnerRequest.isTranscriptProvided?.let {
            ChannelUpdateCommand.ReplaceIsTranscriptProvided(id, it)
        }

    fun updateEducationalResources(): ChannelUpdateCommand.ReplaceEducationalResources? =
        contentPartnerRequest.educationalResources?.let {
            ChannelUpdateCommand.ReplaceEducationalResources(id, it)
        }

    fun updateCurriculumAligned(): ChannelUpdateCommand.ReplaceCurriculumAligned? =
        contentPartnerRequest.curriculumAligned?.let {
            ChannelUpdateCommand.ReplaceCurriculumAligned(id, it)
        }

    fun updateBestForTags(): ChannelUpdateCommand.ReplaceBestForTags? =
        contentPartnerRequest.bestForTags?.let {
            ChannelUpdateCommand.ReplaceBestForTags(id, it)
        }

    fun updateSubjects(): ChannelUpdateCommand.ReplaceSubjects? =
        contentPartnerRequest.subjects?.let {
            ChannelUpdateCommand.ReplaceSubjects(id, it)
        }

    fun updateIngestDetails(): ChannelUpdateCommand.ReplaceIngestDetails? =
        contentPartnerRequest.ingest?.let {
            ChannelUpdateCommand.ReplaceIngestDetails(id, ingestDetailsResourceConverter.fromResource(it))
        }

    fun updateDeliveryFrequency(): ChannelUpdateCommand.ReplaceDeliveryFrequency? =
        contentPartnerRequest.deliveryFrequency?.let {
            ChannelUpdateCommand.ReplaceDeliveryFrequency(id, it)
        }

    fun updateContract(contentPartnerContractRepository: ContentPartnerContractRepository): ChannelUpdateCommand.ReplaceContract? =
        contentPartnerRequest.contractId?.let {
            val contractId = ContentPartnerContractId(it)

            val contract = contentPartnerContractRepository.findById(contractId)
                ?: throw InvalidContractException(contractId)

            ChannelUpdateCommand.ReplaceContract(id, contract)
        }
}

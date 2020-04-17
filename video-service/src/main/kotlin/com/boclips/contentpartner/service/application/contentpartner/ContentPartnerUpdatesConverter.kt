package com.boclips.contentpartner.service.application.contentpartner

import com.boclips.contentpartner.service.application.exceptions.InvalidContractException
import com.boclips.contentpartner.service.domain.model.agerange.AgeRangeBuckets
import com.boclips.contentpartner.service.domain.model.agerange.AgeRangeId
import com.boclips.contentpartner.service.domain.model.agerange.AgeRangeRepository
import com.boclips.contentpartner.service.domain.model.contentpartner.ContentPartnerId
import com.boclips.contentpartner.service.domain.model.contentpartner.ContentPartnerUpdateCommand
import com.boclips.contentpartner.service.domain.model.contentpartner.ContentPartnerUpdateCommand.ReplaceDistributionMethods
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

class ContentPartnerUpdatesConverter(
    private val legalRestrictionsRepository: LegalRestrictionsRepository,
    private val ageRangeRepository: AgeRangeRepository,
    private val ingestDetailsResourceConverter: IngestDetailsResourceConverter,
    private val contentPartnerContractRepository: ContentPartnerContractRepository
) {
    fun convert(
        id: ContentPartnerId,
        upsertContentPartnerRequest: ContentPartnerRequest
    ): List<ContentPartnerUpdateCommand> =
        ContentPartnerUpdateCommandCreator(
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

class ContentPartnerUpdateCommandCreator(
    val id: ContentPartnerId,
    private val contentPartnerRequest: ContentPartnerRequest,
    private val ingestDetailsResourceConverter: IngestDetailsResourceConverter
) {

    fun updateHiddenDeliveryMethods(): ContentPartnerUpdateCommand? {
        return contentPartnerRequest.distributionMethods
            ?.let {
                DistributionMethodResourceConverter.toDistributionMethods(it)
            }
            ?.let { deliveryMethods ->
                ReplaceDistributionMethods(contentPartnerId = id, distributionMethods = deliveryMethods)
            }
    }

    fun updateName(): ContentPartnerUpdateCommand.ReplaceName? =
        contentPartnerRequest.name?.let {
            ContentPartnerUpdateCommand.ReplaceName(contentPartnerId = id, name = it)
        }

    fun updateAgeRanges(ageRangeRepository: AgeRangeRepository): ContentPartnerUpdateCommand.ReplaceAgeRanges? =
        contentPartnerRequest.ageRanges?.let {
            val ageRanges = it.mapNotNull { ageRangeId ->
                ageRangeRepository.findById(
                    AgeRangeId(
                        ageRangeId
                    )
                )
            }
            ContentPartnerUpdateCommand.ReplaceAgeRanges(
                id,
                AgeRangeBuckets(ageRanges = ageRanges)
            )
        }

    fun updateLegalRestrictions(legalRestrictionsRepository: LegalRestrictionsRepository): ContentPartnerUpdateCommand.ReplaceLegalRestrictions? =
        contentPartnerRequest.legalRestrictions
            ?.let { restrictionsRequest ->
                legalRestrictionsRepository.findById(
                    LegalRestrictionsId(
                        restrictionsRequest.id!!
                    )
                )
            }
            ?.let { restrictions -> ContentPartnerUpdateCommand.ReplaceLegalRestrictions(id, restrictions) }

    fun updateCurrency(): ContentPartnerUpdateCommand.ReplaceCurrency? =
        contentPartnerRequest.currency?.let {
            ContentPartnerUpdateCommand.ReplaceCurrency(id, Currency.getInstance(it))
        }

    fun updateContentPartnerTypes(): ContentPartnerUpdateCommand.ReplaceContentTypes? =
        contentPartnerRequest.contentTypes?.let { contentTypes ->
            ContentPartnerUpdateCommand.ReplaceContentTypes(id, contentTypes.mapNotNull { it })
        }

    fun updateContentContentCategories(): ContentPartnerUpdateCommand.ReplaceContentCategories? =
        contentPartnerRequest.contentCategories?.let { contentCategories ->
            ContentPartnerUpdateCommand.ReplaceContentCategories(id, contentCategories)
        }

    fun updateContentLanguage(): ContentPartnerUpdateCommand.ReplaceLanguage? =
        contentPartnerRequest.language?.let { language ->
            ContentPartnerUpdateCommand.ReplaceLanguage(id, language)
        }

    fun updateDescription(): ContentPartnerUpdateCommand.ReplaceDescription? =
        contentPartnerRequest.description?.let { description ->
            ContentPartnerUpdateCommand.ReplaceDescription(id, description)
        }

    fun updateAwards(): ContentPartnerUpdateCommand.ReplaceAwards? =
        contentPartnerRequest.awards?.let { awards ->
            ContentPartnerUpdateCommand.ReplaceAwards(id, awards)
        }

    fun updateHubspotId(): ContentPartnerUpdateCommand.ReplaceHubspotId? =
        contentPartnerRequest.hubspotId?.let { hubspotId ->
            ContentPartnerUpdateCommand.ReplaceHubspotId(id, hubspotId)
        }

    fun updateNotes(): ContentPartnerUpdateCommand.ReplaceNotes? =
        contentPartnerRequest.notes?.let { notes ->
            ContentPartnerUpdateCommand.ReplaceNotes(id, notes)
        }

    fun updateOneLineDescription(): ContentPartnerUpdateCommand.ReplaceOneLineDescription? =
        contentPartnerRequest.oneLineDescription?.let {
            ContentPartnerUpdateCommand.ReplaceOneLineDescription(id, it)
        }

    fun updateMarketingStatus(): ContentPartnerUpdateCommand.ReplaceMarketingStatus? =
        contentPartnerRequest.marketingInformation?.status?.let {
            ContentPartnerUpdateCommand.ReplaceMarketingStatus(id, ContentPartnerMarketingStatusConverter.convert(it))
        }

    fun updateMarketingLogos(): ContentPartnerUpdateCommand.ReplaceMarketingLogos? =
        contentPartnerRequest.marketingInformation?.logos?.let {
            ContentPartnerUpdateCommand.ReplaceMarketingLogos(id, it.map(UrlConverter::convert))
        }

    fun updateMarketingShowreel(): ContentPartnerUpdateCommand.ReplaceMarketingShowreel? =
        contentPartnerRequest.marketingInformation?.showreel?.let {
            ContentPartnerUpdateCommand.ReplaceMarketingShowreel(
                id,
                when (it) {
                    is Specified -> UrlConverter.convert(it.value)
                    is ExplicitlyNull -> null
                }
            )
        }

    fun updateMarketingSampleVideos(): ContentPartnerUpdateCommand.ReplaceMarketingSampleVideos? =
        contentPartnerRequest.marketingInformation?.sampleVideos?.let {
            ContentPartnerUpdateCommand.ReplaceMarketingSampleVideos(id, it.map(UrlConverter::convert))
        }

    fun updateIsTranscriptProvided(): ContentPartnerUpdateCommand.ReplaceIsTranscriptProvided? =
        contentPartnerRequest.isTranscriptProvided?.let {
            ContentPartnerUpdateCommand.ReplaceIsTranscriptProvided(id, it)
        }

    fun updateEducationalResources(): ContentPartnerUpdateCommand.ReplaceEducationalResources? =
        contentPartnerRequest.educationalResources?.let {
            ContentPartnerUpdateCommand.ReplaceEducationalResources(id, it)
        }

    fun updateCurriculumAligned(): ContentPartnerUpdateCommand.ReplaceCurriculumAligned? =
        contentPartnerRequest.curriculumAligned?.let {
            ContentPartnerUpdateCommand.ReplaceCurriculumAligned(id, it)
        }

    fun updateBestForTags(): ContentPartnerUpdateCommand.ReplaceBestForTags? =
        contentPartnerRequest.bestForTags?.let {
            ContentPartnerUpdateCommand.ReplaceBestForTags(id, it)
        }

    fun updateSubjects(): ContentPartnerUpdateCommand.ReplaceSubjects? =
        contentPartnerRequest.subjects?.let {
            ContentPartnerUpdateCommand.ReplaceSubjects(id, it)
        }

    fun updateIngestDetails(): ContentPartnerUpdateCommand.ReplaceIngestDetails? =
        contentPartnerRequest.ingest?.let {
            ContentPartnerUpdateCommand.ReplaceIngestDetails(id, ingestDetailsResourceConverter.fromResource(it))
        }

    fun updateDeliveryFrequency(): ContentPartnerUpdateCommand.ReplaceDeliveryFrequency? =
        contentPartnerRequest.deliveryFrequency?.let {
            ContentPartnerUpdateCommand.ReplaceDeliveryFrequency(id, it)
        }

    fun updateContract(contentPartnerContractRepository: ContentPartnerContractRepository): ContentPartnerUpdateCommand.ReplaceContract? =
        contentPartnerRequest.contractId?.let {
            val contractId = ContentPartnerContractId(it)

            val contract = contentPartnerContractRepository.findById(contractId)
                ?: throw InvalidContractException(contractId)

            ContentPartnerUpdateCommand.ReplaceContract(id, contract)
        }
}

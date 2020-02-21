package com.boclips.contentpartner.service.application

import com.boclips.contentpartner.service.domain.model.AgeRangeBuckets
import com.boclips.contentpartner.service.domain.model.AgeRangeId
import com.boclips.contentpartner.service.domain.model.AgeRangeRepository
import com.boclips.contentpartner.service.domain.model.ContentPartnerId
import com.boclips.contentpartner.service.domain.model.ContentPartnerUpdateCommand
import com.boclips.contentpartner.service.domain.model.ContentPartnerUpdateCommand.ReplaceDistributionMethods
import com.boclips.contentpartner.service.domain.model.LegalRestrictionsId
import com.boclips.contentpartner.service.domain.model.LegalRestrictionsRepository
import com.boclips.contentpartner.service.presentation.ContentPartnerMarketingStatusConverter
import com.boclips.contentpartner.service.presentation.ContentPartnerUrlConverter
import com.boclips.contentpartner.service.presentation.DistributionMethodResourceConverter
import com.boclips.videos.api.common.ExplicitlyNull
import com.boclips.videos.api.common.Specified
import com.boclips.videos.api.request.contentpartner.UpsertContentPartnerRequest
import java.util.Currency

class ContentPartnerUpdatesConverter(
    private val legalRestrictionsRepository: LegalRestrictionsRepository,
    private val ageRangeRepository: AgeRangeRepository
) {
    fun convert(
        id: ContentPartnerId,
        upsertContentPartnerRequest: UpsertContentPartnerRequest
    ): List<ContentPartnerUpdateCommand> =
        ContentPartnerUpdateCommandCreator(id, upsertContentPartnerRequest).let { commandCreator ->
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
                commandCreator.updateSubjects()
            )
        }
}

class ContentPartnerUpdateCommandCreator(
    val id: ContentPartnerId,
    private val upsertContentPartnerRequest: UpsertContentPartnerRequest
) {

    fun updateHiddenDeliveryMethods(): ContentPartnerUpdateCommand? {
        return upsertContentPartnerRequest.distributionMethods
            ?.let {
                DistributionMethodResourceConverter.toDistributionMethods(it)
            }
            ?.let { deliveryMethods ->
                ReplaceDistributionMethods(contentPartnerId = id, distributionMethods = deliveryMethods)
            }
    }

    fun updateName(): ContentPartnerUpdateCommand.ReplaceName? =
        upsertContentPartnerRequest.name?.let {
            ContentPartnerUpdateCommand.ReplaceName(contentPartnerId = id, name = it)
        }

    fun updateAgeRanges(ageRangeRepository: AgeRangeRepository): ContentPartnerUpdateCommand.ReplaceAgeRanges? =
        upsertContentPartnerRequest.ageRanges?.let {
            val ageRanges = it.mapNotNull { ageRangeId ->
                ageRangeRepository.findById(AgeRangeId(ageRangeId))
            }
            ContentPartnerUpdateCommand.ReplaceAgeRanges(id, AgeRangeBuckets(ageRanges = ageRanges))
        }

    fun updateLegalRestrictions(legalRestrictionsRepository: LegalRestrictionsRepository): ContentPartnerUpdateCommand.ReplaceLegalRestrictions? =
        upsertContentPartnerRequest.legalRestrictions
            ?.let { restrictionsRequest -> legalRestrictionsRepository.findById(LegalRestrictionsId(restrictionsRequest.id!!)) }
            ?.let { restrictions -> ContentPartnerUpdateCommand.ReplaceLegalRestrictions(id, restrictions) }

    fun updateCurrency(): ContentPartnerUpdateCommand.ReplaceCurrency? =
        upsertContentPartnerRequest.currency?.let {
            ContentPartnerUpdateCommand.ReplaceCurrency(id, Currency.getInstance(it))
        }

    fun updateContentPartnerTypes(): ContentPartnerUpdateCommand.ReplaceContentTypes? =
        upsertContentPartnerRequest.contentTypes?.let { contentTypes ->
            ContentPartnerUpdateCommand.ReplaceContentTypes(id, contentTypes)
        }

    fun updateContentContentCategories(): ContentPartnerUpdateCommand.ReplaceContentCategories? =
        upsertContentPartnerRequest.contentCategories?.let { contentCategories ->
            ContentPartnerUpdateCommand.ReplaceContentCategories(id, contentCategories)
        }

    fun updateContentLanguage(): ContentPartnerUpdateCommand.ReplaceLanguage? =
        upsertContentPartnerRequest.language?.let { language ->
            ContentPartnerUpdateCommand.ReplaceLanguage(id, language)
        }

    fun updateDescription(): ContentPartnerUpdateCommand.ReplaceDescription? =
        upsertContentPartnerRequest.description?.let { description ->
            ContentPartnerUpdateCommand.ReplaceDescription(id, description)
        }

    fun updateAwards(): ContentPartnerUpdateCommand.ReplaceAwards? =
        upsertContentPartnerRequest.awards?.let { awards ->
            ContentPartnerUpdateCommand.ReplaceAwards(id, awards)
        }

    fun updateHubspotId(): ContentPartnerUpdateCommand.ReplaceHubspotId? =
        upsertContentPartnerRequest.hubspotId?.let { hubspotId ->
            ContentPartnerUpdateCommand.ReplaceHubspotId(id, hubspotId)
        }

    fun updateNotes(): ContentPartnerUpdateCommand.ReplaceNotes? =
        upsertContentPartnerRequest.notes?.let { notes ->
            ContentPartnerUpdateCommand.ReplaceNotes(id, notes)
        }

    fun updateOneLineDescription(): ContentPartnerUpdateCommand.ReplaceOneLineDescription? =
        upsertContentPartnerRequest.oneLineDescription?.let {
            ContentPartnerUpdateCommand.ReplaceOneLineDescription(id, it)
        }

    fun updateMarketingStatus(): ContentPartnerUpdateCommand.ReplaceMarketingStatus? =
        upsertContentPartnerRequest.marketingInformation?.status?.let {
            ContentPartnerUpdateCommand.ReplaceMarketingStatus(id, ContentPartnerMarketingStatusConverter.convert(it))
        }

    fun updateMarketingLogos(): ContentPartnerUpdateCommand.ReplaceMarketingLogos? =
        upsertContentPartnerRequest.marketingInformation?.logos?.let {
            ContentPartnerUpdateCommand.ReplaceMarketingLogos(id, it.map(ContentPartnerUrlConverter::convert))
        }

    fun updateMarketingShowreel(): ContentPartnerUpdateCommand.ReplaceMarketingShowreel? =
        upsertContentPartnerRequest.marketingInformation?.showreel?.let {
            ContentPartnerUpdateCommand.ReplaceMarketingShowreel(
                id,
                when (it) {
                    is Specified -> ContentPartnerUrlConverter.convert(it.value)
                    is ExplicitlyNull -> null
                }
            )
        }

    fun updateMarketingSampleVideos(): ContentPartnerUpdateCommand.ReplaceMarketingSampleVideos? =
        upsertContentPartnerRequest.marketingInformation?.sampleVideos?.let {
            ContentPartnerUpdateCommand.ReplaceMarketingSampleVideos(id, it.map(ContentPartnerUrlConverter::convert))
        }

    fun updateIsTranscriptProvided(): ContentPartnerUpdateCommand.ReplaceIsTranscriptProvided? =
        upsertContentPartnerRequest.isTranscriptProvided?.let {
            ContentPartnerUpdateCommand.ReplaceIsTranscriptProvided(id, it)
        }

    fun updateEducationalResources(): ContentPartnerUpdateCommand.ReplaceEducationalResources? =
        upsertContentPartnerRequest.educationalResources?.let {
            ContentPartnerUpdateCommand.ReplaceEducationalResources(id, it)
        }

    fun updateCurriculumAligned(): ContentPartnerUpdateCommand.ReplaceCurriculumAligned? =
        upsertContentPartnerRequest.curriculumAligned?.let {
            ContentPartnerUpdateCommand.ReplaceCurriculumAligned(id, it)
        }

    fun updateBestForTags(): ContentPartnerUpdateCommand.ReplaceBestForTags? =
        upsertContentPartnerRequest.bestForTags?.let {
            ContentPartnerUpdateCommand.ReplaceBestForTags(id, it)
        }

    fun updateSubjects(): ContentPartnerUpdateCommand.ReplaceSubjects? =
        upsertContentPartnerRequest.subjects?.let {
            ContentPartnerUpdateCommand.ReplaceSubjects(id, it)
        }
}

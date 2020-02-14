package com.boclips.contentpartner.service.application

import com.boclips.contentpartner.service.application.exceptions.ContentPartnerNotFoundException
import com.boclips.contentpartner.service.domain.model.ContentPartner
import com.boclips.contentpartner.service.domain.model.ContentPartnerId
import com.boclips.contentpartner.service.domain.model.ContentPartnerRepository
import com.boclips.eventbus.EventBus
import com.boclips.eventbus.events.contentpartner.ContentPartnerUpdated
import com.boclips.videos.api.request.contentpartner.LegalRestrictionsRequest
import com.boclips.videos.api.request.contentpartner.UpsertContentPartnerRequest
import org.springframework.stereotype.Component
import com.boclips.eventbus.domain.contentpartner.ContentPartner as EventBusContentPartner
import com.boclips.eventbus.domain.contentpartner.ContentPartnerId as EventBusContentPartnerId

@Component
class UpdateContentPartner(
    private val contentPartnerRepository: ContentPartnerRepository,
    private val contentPartnerUpdatesConverter: ContentPartnerUpdatesConverter,
    private val createLegalRestrictions: CreateLegalRestrictions,
    private val eventBus: EventBus
) {
    operator fun invoke(contentPartnerId: String, upsertRequest: UpsertContentPartnerRequest): ContentPartner {
        val id = ContentPartnerId(value = contentPartnerId)

        upsertRequest.legalRestrictions?.let { legalRestrictionsRequest ->
            if (legalRestrictionsRequest.id.isNullOrEmpty()) {
                val legalRestrictions = createLegalRestrictions(legalRestrictionsRequest.text)
                upsertRequest.legalRestrictions =
                    LegalRestrictionsRequest(id = legalRestrictions.id.value)
            }
        }

        val updateCommands = contentPartnerUpdatesConverter.convert(id, upsertRequest)
        contentPartnerRepository.update(updateCommands)

        val updatedContentPartner = contentPartnerRepository.findById(id)
            ?: throw ContentPartnerNotFoundException("Could not find content partner: $contentPartnerId")

        eventBus.publish(
            ContentPartnerUpdated.builder()
                .contentPartner(
                    EventBusContentPartner.builder()
                        .id(EventBusContentPartnerId(updatedContentPartner.contentPartnerId.value))
                        .name(updatedContentPartner.name)
                        .ageRange(
                            com.boclips.eventbus.domain.AgeRange.builder()
                                .min(updatedContentPartner.ageRangeBuckets.min)
                                .max(updatedContentPartner.ageRangeBuckets.max)
                                .build()
                        )
                        .awards(updatedContentPartner.awards)
                        .description(updatedContentPartner.description)
                        .contentTypes(updatedContentPartner.contentTypes?.map { it.name })
                        .contentCategories(updatedContentPartner.contentCategories)
                        .language(updatedContentPartner.language)
                        .hubspotId(updatedContentPartner.hubspotId)
                        .notes(updatedContentPartner.notes)
                        .legalRestrictions(updatedContentPartner.legalRestriction?.text)
                        .build()
                )
                .build()
        )

        return updatedContentPartner
    }
}



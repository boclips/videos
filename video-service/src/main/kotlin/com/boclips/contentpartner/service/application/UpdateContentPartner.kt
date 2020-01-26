package com.boclips.contentpartner.service.application

import com.boclips.contentpartner.service.application.exceptions.ContentPartnerNotFoundException
import com.boclips.contentpartner.service.domain.model.ContentPartner
import com.boclips.contentpartner.service.domain.model.ContentPartnerId
import com.boclips.contentpartner.service.domain.model.ContentPartnerRepository
import com.boclips.eventbus.EventBus
import com.boclips.eventbus.events.contentpartner.ContentPartnerUpdated
import com.boclips.videos.api.request.contentpartner.CreateContentPartnerRequest
import com.boclips.videos.api.request.contentpartner.LegalRestrictionsRequest
import org.springframework.stereotype.Component

@Component
class UpdateContentPartner(
    private val contentPartnerRepository: ContentPartnerRepository,
    private val contentPartnerUpdatesConverter: ContentPartnerUpdatesConverter,
    private val createLegalRestrictions: CreateLegalRestrictions,
    private val eventBus: EventBus
) {
    operator fun invoke(contentPartnerId: String, createRequest: CreateContentPartnerRequest): ContentPartner {
        val id = ContentPartnerId(value = contentPartnerId)

        createRequest.legalRestrictions?.let { legalRestrictionsRequest ->
            if (legalRestrictionsRequest.id.isNullOrEmpty()) {
                val legalRestrictions = createLegalRestrictions(legalRestrictionsRequest.text)
                createRequest.legalRestrictions =
                    LegalRestrictionsRequest(id = legalRestrictions.id.value)
            }
        }

        val updateCommands = contentPartnerUpdatesConverter.convert(id, createRequest)
        contentPartnerRepository.update(updateCommands)

        val contentPartner = contentPartnerRepository.findById(id)
            ?: throw ContentPartnerNotFoundException(
                "Could not find content partner: ${id.value}"
            )

        eventBus.publish(
            ContentPartnerUpdated.builder()
                .contentPartner(
                    com.boclips.eventbus.domain.contentpartner.ContentPartner.builder()
                        .id(com.boclips.eventbus.domain.contentpartner.ContentPartnerId(contentPartner.contentPartnerId.value))
                        .name(contentPartner.name)
                        .ageRange(
                            com.boclips.eventbus.domain.AgeRange.builder()
                                .min(contentPartner.ageRange.min())
                                .max(contentPartner.ageRange.max())
                                .build()
                        )
                        .legalRestrictions(contentPartner.legalRestriction?.text)
                        .build()
                )
                .build()
        )

        return contentPartner
    }
}

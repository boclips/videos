package com.boclips.contentpartner.service.application.contentpartner

import com.boclips.contentpartner.service.application.exceptions.ContentPartnerNotFoundException
import com.boclips.contentpartner.service.application.exceptions.InvalidContractException
import com.boclips.contentpartner.service.application.legalrestriction.CreateLegalRestrictions
import com.boclips.contentpartner.service.domain.model.contentpartner.ContentPartner
import com.boclips.contentpartner.service.domain.model.contentpartner.ContentPartnerId
import com.boclips.contentpartner.service.domain.model.contentpartner.ContentPartnerRepository
import com.boclips.contentpartner.service.domain.model.contentpartnercontract.ContentPartnerContractId
import com.boclips.contentpartner.service.domain.model.contentpartnercontract.ContentPartnerContractRepository
import com.boclips.contentpartner.service.domain.service.EventConverter
import com.boclips.eventbus.EventBus
import com.boclips.eventbus.events.contentpartner.ContentPartnerUpdated
import com.boclips.videos.api.request.contentpartner.ContentPartnerRequest
import com.boclips.videos.api.request.contentpartner.LegalRestrictionsRequest
import com.boclips.videos.service.domain.service.subject.SubjectRepository
import org.springframework.stereotype.Component

@Component
class UpdateContentPartner(
    private val contentPartnerRepository: ContentPartnerRepository,
    private val contentPartnerUpdatesConverter: ContentPartnerUpdatesConverter,
    private val createLegalRestrictions: CreateLegalRestrictions,
    private val subjectRepository: SubjectRepository,
    private val eventConverter: EventConverter,
    private val eventBus: EventBus
) {
    operator fun invoke(contentPartnerId: String, upsertRequest: ContentPartnerRequest): ContentPartner {
        val id = ContentPartnerId(value = contentPartnerId)

        val allSubjects = subjectRepository.findAll()

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
            ContentPartnerUpdated
                .builder()
                .contentPartner(eventConverter.toContentPartnerPayload(updatedContentPartner, allSubjects))
                .build()
        )

        return updatedContentPartner
    }
}



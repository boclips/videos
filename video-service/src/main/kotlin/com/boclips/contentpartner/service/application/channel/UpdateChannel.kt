package com.boclips.contentpartner.service.application.channel

import com.boclips.contentpartner.service.application.exceptions.ContentPartnerNotFoundException
import com.boclips.contentpartner.service.application.legalrestriction.CreateLegalRestrictions
import com.boclips.contentpartner.service.domain.model.channel.Channel
import com.boclips.contentpartner.service.domain.model.channel.ChannelId
import com.boclips.contentpartner.service.domain.model.channel.ChannelRepository
import com.boclips.contentpartner.service.domain.service.EventConverter
import com.boclips.eventbus.EventBus
import com.boclips.eventbus.events.contentpartner.ContentPartnerUpdated
import com.boclips.videos.api.request.channel.ChannelRequest
import com.boclips.videos.api.request.channel.LegalRestrictionsRequest
import com.boclips.videos.service.domain.service.subject.SubjectRepository
import org.springframework.stereotype.Component

@Component
class UpdateChannel(
    private val channelRepository: ChannelRepository,
    private val channelUpdatesConverter: ChannelUpdatesConverter,
    private val createLegalRestrictions: CreateLegalRestrictions,
    private val subjectRepository: SubjectRepository,
    private val eventConverter: EventConverter,
    private val eventBus: EventBus
) {
    operator fun invoke(channelId: String, upsertRequest: ChannelRequest): Channel {
        val id = ChannelId(value = channelId)

        val allSubjects = subjectRepository.findAll()

        upsertRequest.legalRestrictions?.let { legalRestrictionsRequest ->
            if (legalRestrictionsRequest.id.isNullOrEmpty()) {
                val legalRestrictions = createLegalRestrictions(legalRestrictionsRequest.text)
                upsertRequest.legalRestrictions =
                    LegalRestrictionsRequest(id = legalRestrictions.id.value)
            }
        }

        val updateCommands = channelUpdatesConverter.convert(id, upsertRequest)
        channelRepository.update(updateCommands)

        val updatedChannel = channelRepository.findById(id)
            ?: throw ContentPartnerNotFoundException("Could not find content partner: $channelId")

        eventBus.publish(
            ContentPartnerUpdated
                .builder()
                .contentPartner(eventConverter.toContentPartnerPayload(updatedChannel, allSubjects))
                .build()
        )

        return updatedChannel
    }
}



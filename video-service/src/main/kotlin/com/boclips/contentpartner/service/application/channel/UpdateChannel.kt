package com.boclips.contentpartner.service.application.channel

import com.boclips.contentpartner.service.application.exceptions.ChannelNotFoundException
import com.boclips.contentpartner.service.application.exceptions.MissingContractException
import com.boclips.contentpartner.service.application.legalrestriction.CreateLegalRestrictions
import com.boclips.contentpartner.service.domain.model.channel.Channel
import com.boclips.contentpartner.service.domain.model.channel.ChannelId
import com.boclips.contentpartner.service.domain.model.channel.SingleChannelUpdate
import com.boclips.contentpartner.service.domain.model.channel.UpdateChannelResult
import com.boclips.contentpartner.service.domain.service.ChannelService
import com.boclips.contentpartner.service.domain.service.EventConverter
import com.boclips.eventbus.EventBus
import com.boclips.eventbus.events.contentpartner.ContentPartnerUpdated
import com.boclips.videos.api.request.channel.ChannelRequest
import com.boclips.videos.api.request.channel.LegalRestrictionsRequest
import com.boclips.videos.service.domain.model.subject.Subject
import com.boclips.videos.service.domain.service.subject.SubjectRepository
import org.springframework.stereotype.Component

@Component
class UpdateChannel(
    private val channelService: ChannelService,
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
        val updateChannelResult = channelService.update(SingleChannelUpdate(id = id, updateCommands = updateCommands))

        return when (updateChannelResult) {
            is UpdateChannelResult.Success -> {
                publishChannelUpdated(updateChannelResult.channel, allSubjects)

                updateChannelResult.channel
            }
            is UpdateChannelResult.ChannelNotFound -> throw ChannelNotFoundException(id)
            is UpdateChannelResult.MissingContract -> throw MissingContractException()
        }
    }

    private fun publishChannelUpdated(
        channel: Channel,
        allSubjects: List<Subject>
    ) {
        eventBus.publish(
            ContentPartnerUpdated
                .builder()
                .contentPartner(eventConverter.toContentPartnerPayload(channel, allSubjects))
                .build()
        )
    }
}



package com.boclips.contentpartner.service.domain.service.channel

import com.boclips.contentpartner.service.domain.model.channel.Channel
import com.boclips.contentpartner.service.domain.model.channel.ChannelRepository
import com.boclips.contentpartner.service.domain.model.channel.ChannelUpdateCommand
import com.boclips.contentpartner.service.domain.service.EventConverter
import com.boclips.eventbus.EventBus
import com.boclips.eventbus.events.contentpartner.ContentPartnerUpdated
import com.boclips.videos.service.domain.service.subject.SubjectRepository

class ChannelRepositoryEventDecorator(
    private val channelRepository: ChannelRepository,
    private val subjectRepository: SubjectRepository,
    private val eventBus: EventBus,
    private val eventConverter: EventConverter
) : ChannelRepository by channelRepository {

    override fun create(channel: Channel): Channel {
        val createdChannel = channelRepository.create(channel)

        publishCreatedEvent(createdChannel)

        return createdChannel
    }

    override fun update(updateCommands: List<ChannelUpdateCommand>): List<Channel> {
        val channels = channelRepository.update(updateCommands)

        channels.forEach { publishChannelUpdated(it) }

        return channels
    }

    private fun publishCreatedEvent(
        channel: Channel
    ) {
        val allSubjects = subjectRepository.findAll()

        eventBus.publish(
            ContentPartnerUpdated
                .builder()
                .contentPartner(
                    eventConverter.toContentPartnerPayload(
                        channel,
                        allSubjects
                    )
                )
                .build()
        )
    }

    private fun publishChannelUpdated(
        channel: Channel
    ) {
        val allSubjects = subjectRepository.findAll()

        eventBus.publish(
            ContentPartnerUpdated
                .builder()
                .contentPartner(eventConverter.toContentPartnerPayload(channel, allSubjects))
                .build()
        )
    }
}

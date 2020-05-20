package com.boclips.contentpartner.service.application.channel

import com.boclips.contentpartner.service.domain.model.channel.ChannelRepository
import com.boclips.contentpartner.service.domain.service.EventConverter
import com.boclips.eventbus.EventBus
import com.boclips.eventbus.events.contentpartner.BroadcastChannelRequested
import com.boclips.videos.service.domain.service.subject.SubjectRepository

class BroadcastChannels(
    private val eventBus: EventBus,
    private val eventConverter: EventConverter,
    private val channelRepository: ChannelRepository,
    private val subjectRepository: SubjectRepository
) {
    operator fun invoke() {
        val channels = channelRepository.findAll()
        val allSubjects = subjectRepository.findAll()
        channels.forEach {
            eventBus.publish(
                BroadcastChannelRequested.builder().channel(
                    eventConverter.toContentPartnerPayload(it, allSubjects)
                ).build()
            )
        }
    }
}

package com.boclips.contentpartner.service.application.contentpartner

import com.boclips.contentpartner.service.domain.model.contentpartner.ContentPartnerRepository
import com.boclips.contentpartner.service.domain.service.EventConverter
import com.boclips.eventbus.EventBus
import com.boclips.eventbus.events.contentpartner.BroadcastChannelRequested
import com.boclips.videos.service.domain.service.subject.SubjectRepository

class BroadcastChannels(
    private val eventBus: EventBus,
    private val eventConverter: EventConverter,
    private val contentPartnerRepository: ContentPartnerRepository,
    private val subjectRepository: SubjectRepository
) {
    operator fun invoke() {
        val contentPartners = contentPartnerRepository.findAll()
        val allSubjects = subjectRepository.findAll()
        contentPartners.forEach {
            eventBus.publish(
                BroadcastChannelRequested.builder().channel(
                    eventConverter.toContentPartnerPayload(it, allSubjects)
                ).build()
            )
        }
    }
}
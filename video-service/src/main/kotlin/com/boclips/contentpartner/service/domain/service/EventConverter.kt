package com.boclips.contentpartner.service.domain.service

import com.boclips.contentpartner.service.domain.model.ContentPartner
import com.boclips.contentpartner.service.domain.model.CustomIngest
import com.boclips.contentpartner.service.domain.model.IngestDetails
import com.boclips.contentpartner.service.domain.model.ManualIngest
import com.boclips.contentpartner.service.domain.model.MrssFeedIngest
import com.boclips.contentpartner.service.domain.model.YoutubeScrapeIngest
import com.boclips.eventbus.domain.contentpartner.ContentPartnerId
import com.boclips.videos.api.common.IngestType
import com.boclips.eventbus.domain.contentpartner.ContentPartner as EventBusContentPartner
import com.boclips.eventbus.domain.contentpartner.IngestDetails as EventBusIngestDetails

class EventConverter {

    fun toContentPartnerPayload(contentPartner: ContentPartner): EventBusContentPartner {
        return EventBusContentPartner.builder()
            .id(ContentPartnerId(contentPartner.contentPartnerId.value))
            .name(contentPartner.name)
            .ageRange(
                com.boclips.eventbus.domain.AgeRange.builder()
                    .min(contentPartner.ageRangeBuckets.min)
                    .max(contentPartner.ageRangeBuckets.max)
                    .build()
            )
            .awards(contentPartner.awards)
            .description(contentPartner.description)
            .contentTypes(contentPartner.contentTypes?.map { it.name })
            .contentCategories(contentPartner.contentCategories)
            .language(contentPartner.language)
            .hubspotId(contentPartner.hubspotId)
            .notes(contentPartner.notes)
            .legalRestrictions(contentPartner.legalRestriction?.text)
            .ingest(toIngestDetailsPayload(contentPartner.ingest))
            .deliveryFrequency(contentPartner.deliveryFrequency)
            .build()
    }

    fun toIngestDetailsPayload(ingest: IngestDetails): EventBusIngestDetails {
        val (type, urls) = when(ingest) {
            ManualIngest -> IngestType.MANUAL to null
            CustomIngest -> IngestType.CUSTOM to null
            is MrssFeedIngest -> IngestType.MRSS to ingest.urls
            is YoutubeScrapeIngest -> IngestType.YOUTUBE to ingest.playlistIds
        }

        return EventBusIngestDetails
            .builder()
            .type(type.name)
            .urls(urls)
            .build()
    }
}

package com.boclips.videos.service.testsupport

import com.boclips.contentpartner.service.domain.model.channel.*

class ChannelFactory {
    companion object {
        fun create(id: String, name: String): Channel {
            return Channel(
                    id = ChannelId(id),
                    name = name,
                    distributionMethods = emptySet(),
                    ingest = ManualIngest,
                    legalRestriction = null,
                    remittance = null,
                    description = null,
                    contentCategories = null,
                    hubspotId = null,
                    language = null,
                    awards = null,
                    notes = null,
                    contentTypes = null,
                    deliveryFrequency = null,
                    pedagogyInformation = null,
                    marketingInformation = null,
                    contract = null
            )
        }
    }
}

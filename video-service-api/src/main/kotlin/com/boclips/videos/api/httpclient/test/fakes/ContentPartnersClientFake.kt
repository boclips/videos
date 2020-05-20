package com.boclips.videos.api.httpclient.test.fakes

import com.boclips.videos.api.httpclient.ContentPartnersClient
import com.boclips.videos.api.request.channel.ChannelFilterRequest
import com.boclips.videos.api.request.channel.ChannelRequest
import com.boclips.videos.api.response.channel.ChannelResource
import com.boclips.videos.api.response.channel.LegacyContentPartnerWrapperResource
import com.boclips.videos.api.response.channel.LegacyContentPartnersResource

class ContentPartnersClientFake : ContentPartnersClient, FakeClient<ChannelResource> {
    private val database: MutableMap<String, ChannelResource> = LinkedHashMap()
    private var id = 0

    override fun getContentPartners(channelFilterRequest: ChannelFilterRequest): LegacyContentPartnersResource {
        val contentPartners = if (channelFilterRequest.name != null) {
            database.values.toList().filter { it.name == channelFilterRequest.name }
        } else {
            database.values.toList()
        }

        return LegacyContentPartnersResource(_embedded = LegacyContentPartnerWrapperResource(contentPartners))
    }

    override fun getContentPartner(contentPartnerId: String): ChannelResource {
        return database[contentPartnerId]!!
    }

    override fun create(upsertChannelRequest: ChannelRequest) {
        val id = "${id++}"
        database[id] = ChannelResource(
            id = id,
            name = upsertChannelRequest.name!!,
            currency = upsertChannelRequest.currency,
            legalRestriction = null,
            distributionMethods = setOf(),
            official = true
        )
    }

    override fun add(element: ChannelResource): ChannelResource {
        database[element.id] = element
        return element
    }

    override fun findAll(): List<ChannelResource> {
        return database.values.toList()
    }

    override fun clear() {
        database.clear()
    }
}

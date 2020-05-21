package com.boclips.videos.api.httpclient.test.fakes

import com.boclips.videos.api.httpclient.ChannelsClient
import com.boclips.videos.api.request.channel.ChannelFilterRequest
import com.boclips.videos.api.request.channel.ChannelRequest
import com.boclips.videos.api.response.channel.ChannelResource
import com.boclips.videos.api.response.channel.ChannelWrapperResource
import com.boclips.videos.api.response.channel.ChannelsResource

class ChannelsClientFake : ChannelsClient, FakeClient<ChannelResource> {
    private val database: MutableMap<String, ChannelResource> = LinkedHashMap()
    private var id = 0

    override fun getChannels(channelFilterRequest: ChannelFilterRequest): ChannelsResource {
        val channels = if (channelFilterRequest.name != null) {
            database.values.toList().filter { it.name == channelFilterRequest.name }
        } else {
            database.values.toList()
        }

        return ChannelsResource(_embedded = ChannelWrapperResource(channels))
    }

    override fun getChannel(channelId: String): ChannelResource {
        return database[channelId]!!
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

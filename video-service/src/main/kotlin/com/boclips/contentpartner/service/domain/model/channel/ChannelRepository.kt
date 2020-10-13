package com.boclips.contentpartner.service.domain.model.channel

import com.boclips.contentpartner.service.domain.model.contract.ContractId
import com.boclips.videos.service.domain.model.suggestions.ChannelSuggestion

interface ChannelRepository {
    fun create(channel: Channel): Channel
    fun findAll(): Iterable<Channel>
    fun findAll(filters: List<ChannelFilter>): Iterable<Channel>
    fun findAllByIds(ids: List<ChannelId>): Iterable<Channel>
    fun findById(channelId: ChannelId): Channel?
    fun findByContractId(contractId: ContractId): List<Channel>
    fun findByName(query: String): List<Channel>
    fun update(updateCommands: List<ChannelUpdateCommand>): List<Channel>
    fun streamAll(consumer: (Sequence<ChannelSuggestion>) -> Unit)
}

package com.boclips.contentpartner.service.domain.model.channel

import com.boclips.contentpartner.service.domain.model.contract.ContractId

interface ChannelRepository {
    fun create(channel: Channel): Channel
    fun findAll(): Iterable<Channel>
    fun findAll(filters: List<ChannelFilter>): Iterable<Channel>
    fun findById(channelId: ChannelId): Channel?
    fun findByContractId(contractId: ContractId): List<Channel>
    fun findByName(query: String): List<Channel>
    fun update(updateCommands: List<ChannelUpdateCommand>)
}

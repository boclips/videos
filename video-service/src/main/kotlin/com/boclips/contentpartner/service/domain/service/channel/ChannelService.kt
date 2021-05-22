package com.boclips.contentpartner.service.domain.service.channel

import com.boclips.contentpartner.service.application.channel.ChannelFiltersConverter
import com.boclips.contentpartner.service.common.PageInfo
import com.boclips.contentpartner.service.common.ResultsPage
import com.boclips.contentpartner.service.domain.model.channel.Channel
import com.boclips.contentpartner.service.domain.model.channel.ChannelId
import com.boclips.contentpartner.service.domain.model.channel.ChannelRepository
import com.boclips.contentpartner.service.domain.model.channel.ChannelRequest
import com.boclips.contentpartner.service.domain.model.channel.ChannelUpdateCommand
import com.boclips.contentpartner.service.domain.model.channel.CreateChannelResult
import com.boclips.contentpartner.service.domain.model.channel.SingleChannelUpdate
import com.boclips.contentpartner.service.domain.model.channel.UpdateChannelResult
import com.boclips.search.service.domain.common.model.PaginatedIndexSearchRequest
import com.boclips.videos.api.common.IngestType
import com.boclips.videos.service.domain.model.suggestions.ChannelSuggestion
import com.boclips.videos.service.domain.service.suggestions.ChannelIndex

class ChannelService(
    private val channelRepository: ChannelRepository,
    private val channelIndex: ChannelIndex
) {
    fun create(channel: Channel): CreateChannelResult {
        if (hasNameConflict(channel)) {
            return CreateChannelResult.NameConflict(channel.name)
        }

        if (isMissingContract(channel)) {
            return CreateChannelResult.MissingContract
        }

        val persistedChannel = channelRepository.create(channel)

        channelIndex.upsert(
            sequenceOf(
                ChannelSuggestion(
                    name = channel.name,
                    id = ChannelId(channel.id.value),
                    eligibleForStream = channel.isStreamable(),
                    contentTypes = channel.contentTypes ?: emptyList(),
                    taxonomy = channel.taxonomy
                )
            )
        )

        return CreateChannelResult.Success(persistedChannel)
    }

    fun update(update: SingleChannelUpdate): UpdateChannelResult {
        if (isMissingContract(update)) {
            return UpdateChannelResult.MissingContract(update.id)
        }

        channelRepository.update(update.updateCommands)

        val channel = channelRepository.findById(update.id)

        return if (channel != null) {
            UpdateChannelResult.Success(channel = channel)
        } else {
            UpdateChannelResult.ChannelNotFound(channelId = update.id)
        }
    }

    private fun isMissingContract(channel: Channel) =
        channel.ingest.type() != IngestType.YOUTUBE && channel.contract == null

    private fun hasNameConflict(channel: Channel): Boolean =
        channelRepository.findAll(ChannelFiltersConverter.convert(name = channel.name)).toList().isNotEmpty()

    private fun isMissingContract(update: SingleChannelUpdate): Boolean {
        update.getUpdateByType<ChannelUpdateCommand.ReplaceIngestDetails>()
            ?.takeIf { it.ingest.type() != IngestType.YOUTUBE }
            ?.let {
                val channel = channelRepository.findById(update.id)
                if (channel?.contract == null && update.getUpdateByType<ChannelUpdateCommand.ReplaceContract>() == null) {
                    return true
                }
            }

        return false
    }

    fun search(channelRequest: ChannelRequest): ResultsPage<Channel> {
        val pageRequest = channelRequest.pageRequest

        val searchRequest =
            PaginatedIndexSearchRequest(
                query = channelRequest.toQuery(),
                startIndex = pageRequest.getStartIndex(),
                windowSize = pageRequest.size
            )

        val searchResults = channelIndex.search(searchRequest)
        val foundChannels = channelRepository.findAllByIds(searchResults.elements.map { ChannelId(it) }).toList()

        return ResultsPage(
            elements = foundChannels,
            pageInfo = PageInfo(
                hasMoreElements = (pageRequest.page + 1) * pageRequest.size < searchResults.counts.totalHits,
                totalElements = searchResults.counts.totalHits,
                pageRequest = pageRequest
            )
        )
    }
}

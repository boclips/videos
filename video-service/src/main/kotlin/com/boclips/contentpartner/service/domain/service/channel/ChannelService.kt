package com.boclips.contentpartner.service.domain.service.channel

import com.boclips.contentpartner.service.application.channel.ChannelFiltersConverter
import com.boclips.contentpartner.service.common.PageRequest
import com.boclips.contentpartner.service.domain.model.channel.Channel
import com.boclips.contentpartner.service.domain.model.channel.ChannelId
import com.boclips.contentpartner.service.domain.model.channel.ChannelRepository
import com.boclips.contentpartner.service.domain.model.channel.ChannelSortKey
import com.boclips.contentpartner.service.domain.model.channel.ChannelUpdateCommand
import com.boclips.contentpartner.service.domain.model.channel.CreateChannelResult
import com.boclips.contentpartner.service.domain.model.channel.SingleChannelUpdate
import com.boclips.contentpartner.service.domain.model.channel.UpdateChannelResult
import com.boclips.search.service.domain.channels.model.ChannelMetadata
import com.boclips.search.service.domain.channels.model.ChannelQuery
import com.boclips.search.service.domain.common.model.PaginatedIndexSearchRequest
import com.boclips.search.service.domain.common.model.Sort
import com.boclips.search.service.domain.common.model.SortOrder
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

        if (channel.hubspotId != null && hasHubSpotIdConflict(channel)) {
            return CreateChannelResult.HubSpotIdConflict(channel.hubspotId)
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

    private fun hasHubSpotIdConflict(channel: Channel): Boolean =
        channelRepository.findAll(ChannelFiltersConverter.convert(hubspotId = channel.hubspotId)).toList().isNotEmpty()

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

    fun search(sortBy: ChannelSortKey?, pageRequest: PageRequest?): List<Channel> {
        val sortByField = when (sortBy) {
            ChannelSortKey.CATEGORIES_ASC -> listOf(
                Sort.ByField(
                    fieldName = ChannelMetadata::taxonomy,
                    order = SortOrder.ASC
                )
            )
            ChannelSortKey.CATEGORIES_DESC -> listOf(
                Sort.ByField(
                    fieldName = ChannelMetadata::taxonomy,
                    order = SortOrder.DESC
                )
            )
            null -> emptyList<Sort<ChannelMetadata>>()
        }

        val searchRequest = pageRequest?.let {
            PaginatedIndexSearchRequest(
                query = ChannelQuery(sort = sortByField),
                startIndex = pageRequest.getStartIndex(),
                windowSize = pageRequest.size
            )
        } ?: PaginatedIndexSearchRequest(query = ChannelQuery(sort = sortByField))

        val searchResults = channelIndex.search(searchRequest)
        return channelRepository.findAllByIds(searchResults.elements.map { ChannelId(it) }).toList()
    }
}

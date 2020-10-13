package com.boclips.videos.service.domain.service.suggestions

import com.boclips.contentpartner.service.domain.model.channel.ChannelId
import com.boclips.contentpartner.service.domain.model.channel.DistributionMethod
import com.boclips.search.service.domain.common.model.SearchRequestWithoutPagination
import com.boclips.videos.service.domain.model.suggestions.ChannelSuggestion
import com.boclips.videos.service.domain.model.suggestions.NewSuggestions
import com.boclips.videos.service.domain.model.suggestions.request.ChannelsSuggestionsRequest
import com.boclips.videos.service.domain.model.video.VideoAccess
import com.boclips.videos.service.domain.model.video.VideoAccessRule
import mu.KLogging

class NewSuggestionsRetrievalService(
    private val channelIndex: ChannelIndex,
) {
    companion object : KLogging()

    private val defaultAccessRules = listOf(
        VideoAccessRule.IncludedDistributionMethods(
            setOf(
                DistributionMethod.STREAM
            )
        )
    )

    fun findSuggestions(request: ChannelsSuggestionsRequest, videoAccess: VideoAccess): NewSuggestions {
        val videoAccessWithDefaultRules = withDefaultRules(videoAccess)

        val searchRequest = SearchRequestWithoutPagination(
            query = request.toQuery(videoAccessWithDefaultRules)
        )

        val results = channelIndex.search(searchRequest)

        val channels = results.elements.map {
            ChannelSuggestion(
                name = it.name,
                id = ChannelId(it.id)
            )
        }

        return NewSuggestions(
            channels = channels
        )
    }

    private fun withDefaultRules(videoAccess: VideoAccess): VideoAccess.Rules {
        return when (videoAccess) {
            VideoAccess.Everything -> VideoAccess.Rules(defaultAccessRules)
            is VideoAccess.Rules -> VideoAccess.Rules(videoAccess.accessRules + defaultAccessRules)
        }
    }
}

package com.boclips.videos.service.domain.service.suggestions

import com.boclips.contentpartner.service.domain.model.channel.ChannelId
import com.boclips.contentpartner.service.domain.model.channel.DistributionMethod
import com.boclips.search.service.domain.channels.model.ChannelMetadata
import com.boclips.search.service.domain.common.model.SearchRequestWithoutPagination
import com.boclips.search.service.domain.subjects.model.SubjectMetadata
import com.boclips.videos.service.domain.model.subject.SubjectId
import com.boclips.videos.service.domain.model.suggestions.ChannelSuggestion
import com.boclips.videos.service.domain.model.suggestions.NewSuggestions
import com.boclips.videos.service.domain.model.suggestions.SubjectSuggestion
import com.boclips.videos.service.domain.model.suggestions.request.SuggestionsRequest
import com.boclips.videos.service.domain.model.video.VideoAccess
import com.boclips.videos.service.domain.model.video.VideoAccessRule
import mu.KLogging

class NewSuggestionsRetrievalService(
    private val channelIndex: ChannelIndex,
    private val subjectIndex: SubjectIndex
) {
    companion object : KLogging()

    private val defaultAccessRules = listOf(
        VideoAccessRule.IncludedDistributionMethods(
            setOf(
                DistributionMethod.STREAM
            )
        )
    )

    fun findSuggestions(request: SuggestionsRequest, videoAccess: VideoAccess): NewSuggestions {
        val videoAccessWithDefaultRules = withDefaultRules(videoAccess)

        val channelsQuery = SearchRequestWithoutPagination(
            query = request.toQuery<ChannelMetadata>(videoAccessWithDefaultRules)
        )

        val subjectsQuery = SearchRequestWithoutPagination(
            query = request.toQuery<SubjectMetadata>()
        )

        val channels = channelIndex.search(channelsQuery)
        val subjects = subjectIndex.search(subjectsQuery)

        val channelsResults = channels.elements.map {
            ChannelSuggestion(
                name = it.name,
                id = ChannelId(it.id)
            )
        }

        val subjectsResults = subjects.elements.map {
            SubjectSuggestion(
                name = it.name,
                id = SubjectId(it.id)
            )
        }

        return NewSuggestions(
            channels = channelsResults,
            subjects = subjectsResults
        )
    }

    private fun withDefaultRules(videoAccess: VideoAccess): VideoAccess.Rules {
        return when (videoAccess) {
            VideoAccess.Everything -> VideoAccess.Rules(defaultAccessRules)
            is VideoAccess.Rules -> VideoAccess.Rules(videoAccess.accessRules + defaultAccessRules)
        }
    }
}

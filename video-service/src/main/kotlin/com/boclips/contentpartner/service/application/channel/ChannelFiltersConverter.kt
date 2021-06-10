package com.boclips.contentpartner.service.application.channel

import com.boclips.contentpartner.service.domain.model.channel.ChannelFilter
import com.boclips.videos.api.common.IngestType

object ChannelFiltersConverter {
    fun convert(
        name: String? = null,
        ingestTypes: List<IngestType>? = null,
        private: Boolean? = null
    ): List<ChannelFilter> =
        listOfNotNull(
            getNameFilter(
                name
            ),
            getIngestTypesFilter(
                ingestTypes
            ),
            getPrivateFilter(
                private
            )
        )

    private fun getNameFilter(name: String?) =
        name?.let { ChannelFilter.NameFilter(name = it) }

    private fun getPrivateFilter(private: Boolean?) =
        private?.let { ChannelFilter.PrivateFilter(private = it) }

    private fun getIngestTypesFilter(ingestTypes: List<IngestType>?) =
        ingestTypes?.let { ChannelFilter.IngestTypesFilter(ingestTypes = it) }
}

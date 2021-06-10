package com.boclips.contentpartner.service.application.channel

import com.boclips.contentpartner.service.domain.model.channel.ChannelFilter
import com.boclips.videos.api.common.IngestType

object ChannelFiltersConverter {
    fun convert(
        name: String? = null,
        ingestTypes: List<IngestType>? = null,
        hidden: Boolean? = null
    ): List<ChannelFilter> =
        listOfNotNull(
            getNameFilter(
                name
            ),
            getIngestTypesFilter(
                ingestTypes
            ),
            getHiddenFilter(
                hidden
            )
        )

    private fun getNameFilter(name: String?) =
        name?.let { ChannelFilter.NameFilter(name = it) }

    private fun getHiddenFilter(hidden: Boolean?) =
        hidden?.let { ChannelFilter.HiddenFilter(hidden = it) }

    private fun getIngestTypesFilter(ingestTypes: List<IngestType>?) =
        ingestTypes?.let { ChannelFilter.IngestTypesFilter(ingestTypes = it) }
}

package com.boclips.contentpartner.service.presentation.converters

import ChannelTaxonomyResource
import com.boclips.contentpartner.service.domain.model.channel.Taxonomy
import com.boclips.videos.api.response.channel.TaxonomyCategoryResource

object TaxonomyResourceConverter {
    fun toResource(taxonomy: Taxonomy?): ChannelTaxonomyResource? = when (taxonomy) {
        is Taxonomy.VideoLevelTagging ->
            ChannelTaxonomyResource(
                requiresVideoLevelTagging = true
            )
        is Taxonomy.ChannelLevelTagging -> ChannelTaxonomyResource(
            categories = taxonomy.categories.map { category ->
                TaxonomyCategoryResource(codeValue = category.codeValue.value, description = category.description)
            }
        )
        else -> null
    }
}

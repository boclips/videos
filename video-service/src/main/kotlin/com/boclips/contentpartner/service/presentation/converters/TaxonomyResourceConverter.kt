package com.boclips.contentpartner.service.presentation.converters

import TaxonomyResource
import com.boclips.contentpartner.service.domain.model.channel.Taxonomy
import com.boclips.videos.api.response.channel.TaxonomyCategoryResource

object TaxonomyResourceConverter {
    fun toResource(taxonomy: Taxonomy?): TaxonomyResource? = when (taxonomy) {
        is Taxonomy.VideoLevelTagging ->
            TaxonomyResource(
                requiresVideoLevelTagging = true
            )
        is Taxonomy.ChannelLevelTagging -> TaxonomyResource(
            categories = taxonomy.categories.map { category ->
                TaxonomyCategoryResource(codeValue = category.codeValue.value, description = category.description)
            }
        )
        else -> null
    }
}

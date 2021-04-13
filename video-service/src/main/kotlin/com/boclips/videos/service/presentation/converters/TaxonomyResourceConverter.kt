package com.boclips.videos.service.presentation.converters

import com.boclips.videos.api.response.taxonomy.TaxonomyResource
import com.boclips.videos.api.response.taxonomy.TaxonomyTreeResource
import com.boclips.videos.service.domain.model.taxonomy.Categories
import com.boclips.videos.service.domain.model.taxonomy.Category

object TaxonomyResourceConverter {

    fun toResource(categories: Categories): TaxonomyResource {
        return TaxonomyResource(
            _embedded = categories.map { parentTaxonomy ->
                parentTaxonomy.key to
                    convertTaxonomyTree(parentTaxonomy.value)
            }.toMap()
        )
    }

    private fun convertTaxonomyTree(tree: Category): TaxonomyTreeResource {
        if (tree.children.isNotEmpty()) {
            return TaxonomyTreeResource(
                children = tree.children.map { child ->
                    child.key to convertTaxonomyTree(child.value)
                }.toMap(),
                description = tree.description
            )
        }
        return TaxonomyTreeResource(description = tree.description, children = emptyMap())
    }
}

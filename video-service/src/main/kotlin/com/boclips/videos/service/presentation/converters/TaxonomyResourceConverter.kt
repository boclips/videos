package com.boclips.videos.service.presentation.converters

import com.boclips.videos.api.response.taxonomy.TaxonomyResource
import com.boclips.videos.api.response.taxonomy.TaxonomyTreeResource
import com.boclips.videos.service.domain.model.taxonomy.Category

object TaxonomyResourceConverter {

    fun toResource(categories: List<Category>): TaxonomyResource {
        return TaxonomyResource(
            _embedded = categories.map { parentTaxonomy ->
                parentTaxonomy.code.value to
                    convertTaxonomyTree(parentTaxonomy)
            }.toMap()
        )
    }

    private fun convertTaxonomyTree(tree: Category): TaxonomyTreeResource {
        if (tree.children.isNotEmpty()) {
            return TaxonomyTreeResource(
                children = tree.children.map { child ->
                    child.key.value to convertTaxonomyTree(child.value)
                }.toMap(),
                description = tree.description
            )
        }
        return TaxonomyTreeResource(description = tree.description, children = emptyMap())
    }
}

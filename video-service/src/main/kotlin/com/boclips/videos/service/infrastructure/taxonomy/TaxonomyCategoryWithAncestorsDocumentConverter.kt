package com.boclips.videos.service.infrastructure.video.converters

import com.boclips.videos.service.domain.model.taxonomy.CategoryCode
import com.boclips.videos.service.domain.model.taxonomy.TaxonomyCategoryWithAncestors
import com.boclips.videos.service.infrastructure.taxonomy.TaxonomyCategoryWithAncestorsDocument

object TaxonomyCategoryWithAncestorsDocumentConverter {
    fun toTaxonomyDocument(taxonomyCategory: TaxonomyCategoryWithAncestors): TaxonomyCategoryWithAncestorsDocument =
        TaxonomyCategoryWithAncestorsDocument(
            codeValue = taxonomyCategory.codeValue.value,
            description = taxonomyCategory.description,
            ancestors = taxonomyCategory.ancestors.map { it.value }.toSet()
        )

    fun toTaxonomy(document: TaxonomyCategoryWithAncestorsDocument): TaxonomyCategoryWithAncestors {
        return TaxonomyCategoryWithAncestors(
            codeValue = CategoryCode(document.codeValue),
            description = document.description,
            ancestors = document.ancestors.map { CategoryCode(it) }.toList()
        )
    }
}

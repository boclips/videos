package com.boclips.videos.service.infrastructure.video.converters

import com.boclips.videos.service.domain.model.taxonomy.TaxonomyCategoryWithAncestors
import com.boclips.videos.service.infrastructure.taxonomy.TaxonomyCategoryWithAncestorsDocument

object TaxonomyCategoryWithAncestorsDocumentConverter {
    fun toTaxonomyDocument(taxonomyCategory: TaxonomyCategoryWithAncestors): TaxonomyCategoryWithAncestorsDocument =
        TaxonomyCategoryWithAncestorsDocument(
            codeValue = taxonomyCategory.codeValue,
            description = taxonomyCategory.description,
            ancestors = taxonomyCategory.ancestors
        )

    fun toTaxonomy(document: TaxonomyCategoryWithAncestorsDocument): TaxonomyCategoryWithAncestors {
        return TaxonomyCategoryWithAncestors(
            codeValue = document.codeValue,
            description = document.description,
            ancestors = document.ancestors
        )
    }
}

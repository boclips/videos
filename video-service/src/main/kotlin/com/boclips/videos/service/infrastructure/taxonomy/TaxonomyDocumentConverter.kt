package com.boclips.videos.service.infrastructure.video.converters

import com.boclips.videos.service.domain.model.taxonomy.TaxonomyCategory
import com.boclips.videos.service.infrastructure.taxonomy.TaxonomyCategoryDocument
import org.bson.types.ObjectId

object TaxonomyDocumentConverter {
    fun toTaxonomyDocument(taxonomyCategory: TaxonomyCategory): TaxonomyCategoryDocument =
        TaxonomyCategoryDocument(
            id = ObjectId(),
            codeValue = taxonomyCategory.codeValue,
            codeDescription = taxonomyCategory.description,
            codeParent = taxonomyCategory.parentCode
        )

    fun toTaxonomy(document: TaxonomyCategoryDocument): TaxonomyCategory {
        return TaxonomyCategory(codeValue = document.codeValue, description = document.codeDescription, parentCode = document.codeParent)
    }
}

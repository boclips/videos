package com.boclips.videos.service.infrastructure.video.converters

import com.boclips.videos.service.domain.model.taxonomy.TaxonomyCategory
import com.boclips.videos.service.infrastructure.taxonomy.TaxonomyDocument
import org.bson.types.ObjectId

object TaxonomyDocumentConverter {
    fun toTaxonomyDocument(taxonomyCategory: TaxonomyCategory): TaxonomyDocument =
            TaxonomyDocument(
                id = ObjectId(),
                codeValue = taxonomyCategory.codeValue,
                codeDescription = taxonomyCategory.description,
                codeParent = taxonomyCategory.parentCode
            )


    fun toTaxonomy(document: TaxonomyDocument): TaxonomyCategory {
        return TaxonomyCategory(codeValue = document.codeValue, description = document.codeDescription, parentCode = document.codeParent)
    }

}


package com.boclips.videos.service.infrastructure.video.converters

import com.boclips.videos.service.domain.model.taxonomy.Taxonomy
import com.boclips.videos.service.infrastructure.taxonomy.TaxonomyDocument
import org.bson.types.ObjectId

object TaxonomyDocumentConverter {
    fun toTaxonomyDocument(taxonomy: Taxonomy): TaxonomyDocument =
            TaxonomyDocument(
                id = ObjectId(),
                codeValue = taxonomy.codeValue,
                codeDescription = taxonomy.description,
                codeParent = taxonomy.parentCode
            )


    fun toTaxonomy(document: TaxonomyDocument): Taxonomy {
        return Taxonomy(codeValue = document.codeValue, description = document.codeDescription, parentCode = document.codeParent)
    }

}


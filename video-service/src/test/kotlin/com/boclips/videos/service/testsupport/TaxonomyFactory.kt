package com.boclips.videos.service.testsupport

import com.boclips.videos.service.domain.model.taxonomy.Taxonomy
import com.boclips.videos.service.domain.model.taxonomy.TaxonomyTree

object TaxonomyFactory {

    fun sample(
        codeValue: String = "A",
        description: String = "taxonomy description",
        parentCode: String? = null
    ) = Taxonomy(
        codeValue = codeValue,
        description = description,
        parentCode = parentCode
    )

    fun sampleTree() = TaxonomyTree
}

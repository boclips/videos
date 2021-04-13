package com.boclips.videos.service.testsupport

import com.boclips.videos.service.domain.model.taxonomy.TaxonomyCategory
import com.boclips.videos.service.domain.model.taxonomy.Category

object TaxonomyFactory {

    fun sample(
        codeValue: String = "A",
        description: String = "taxonomy description",
        parentCode: String? = null
    ) = TaxonomyCategory(
        codeValue = codeValue,
        description = description,
        parentCode = parentCode
    )

    fun sampleTree() = Category(description = "test", code="A", children = emptyMap())
}

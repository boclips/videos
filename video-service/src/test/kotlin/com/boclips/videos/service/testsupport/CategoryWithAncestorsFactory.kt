package com.boclips.videos.service.testsupport

import com.boclips.videos.service.domain.model.taxonomy.CategoryCode
import com.boclips.videos.service.domain.model.taxonomy.CategoryWithAncestors

object CategoryWithAncestorsFactory {

    fun sample(
        codeValue: String = "A",
        description: String = "taxonomy description",
        ancestors: Set<CategoryCode> = emptySet()
    ) = CategoryWithAncestors(
        codeValue = CategoryCode(codeValue),
        description = description,
        ancestors = ancestors
    )
}

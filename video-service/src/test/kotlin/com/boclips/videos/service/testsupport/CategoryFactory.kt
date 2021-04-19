package com.boclips.videos.service.testsupport

import com.boclips.videos.service.domain.model.taxonomy.Category
import com.boclips.videos.service.domain.model.taxonomy.CategoryCode

object CategoryFactory {

    fun sample(
        code: String = "A",
        description: String = "taxonomy description",
        parentCode: String? = null
    ) = Category(
        code = CategoryCode(code),
        description = description,
        parentCode = parentCode?.let { CategoryCode(it) }
    )
}

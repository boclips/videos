package com.boclips.videos.service.domain.service.video

import com.boclips.videos.service.domain.model.taxonomy.Category
import com.boclips.videos.service.domain.model.taxonomy.CategoryCode

interface TaxonomyRepository {
    fun findAll(): List<Category>
    fun findByCode(codeValue: CategoryCode): Category?
    fun findByCode(codeValue: List<CategoryCode>): Category?
    fun create(taxonomyCategory: Category): Category
}

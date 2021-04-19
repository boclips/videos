package com.boclips.videos.service.domain.service.taxonomy

import com.boclips.videos.service.domain.model.taxonomy.Category
import com.boclips.videos.service.domain.model.taxonomy.CategoryCode

interface CategoryRepository {
    fun findAll(): List<Category>
    fun findByCode(code: CategoryCode): Category?
    fun create(category: Category): Category
}

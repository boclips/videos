package com.boclips.videos.service.application

import com.boclips.videos.service.domain.model.taxonomy.Category
import com.boclips.videos.service.domain.service.taxonomy.TaxonomyService
import org.springframework.stereotype.Component

@Component
class GetAllCategories(
    private val taxonomyService: TaxonomyService
) {
    operator fun invoke(): List<Category> {
        return taxonomyService.getCategories()
    }
}

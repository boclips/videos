package com.boclips.videos.service.domain.service.taxonomy

import com.boclips.videos.service.domain.model.taxonomy.Category
import com.boclips.videos.service.domain.service.video.TaxonomyRepository

class TaxonomyService(
    private val taxonomyRepository: TaxonomyRepository
) {

    fun getCategories(): List<Category> {
        return taxonomyRepository.findAll()
    }
}

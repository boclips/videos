package com.boclips.videos.service.domain.service.video

import com.boclips.videos.service.domain.model.taxonomy.TaxonomyCategory

interface TaxonomyRepository {
    fun findAll(): List<TaxonomyCategory>
    fun create(taxonomyCategory: TaxonomyCategory): TaxonomyCategory
}


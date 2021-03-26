package com.boclips.videos.service.domain.service.video

import com.boclips.videos.service.domain.model.taxonomy.Taxonomy

interface TaxonomyRepository {
    fun findAll(): List<Taxonomy>
    fun create(taxonomy: Taxonomy): Taxonomy
}


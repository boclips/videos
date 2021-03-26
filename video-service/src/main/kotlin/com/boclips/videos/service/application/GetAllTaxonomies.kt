package com.boclips.videos.service.application

import com.boclips.videos.service.domain.model.taxonomy.Taxonomy
import com.boclips.videos.service.domain.model.taxonomy.TaxonomyTree
import com.boclips.videos.service.domain.service.video.TaxonomyRepository
import org.springframework.stereotype.Component

@Component
class GetAllTaxonomies(
    private val taxonomyRepository: TaxonomyRepository
) {
    operator fun invoke(): Taxonomy {
        val taxonomies = taxonomyRepository.findAll()
        return TaxonomyTree.buildTaxonomies(taxonomies)
    }
}

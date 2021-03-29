package com.boclips.videos.service.application

import com.boclips.videos.service.domain.model.taxonomy.Taxonomy
import com.boclips.videos.service.domain.service.taxonomy.TaxonomyService
import com.boclips.videos.service.domain.service.video.TaxonomyRepository
import org.springframework.stereotype.Component

@Component
class GetAllTaxonomies(
    private val taxonomyService: TaxonomyService
) {
    operator fun invoke(): Taxonomy {
        return taxonomyService.getTaxonomyTree()
    }
}

package com.boclips.videos.service.application

import com.boclips.videos.service.domain.model.taxonomy.TaxonomyCategory
import com.boclips.videos.service.domain.model.taxonomy.TaxonomyCategoryWithAncestors
import com.boclips.videos.service.domain.service.video.TaxonomyRepository
import org.springframework.stereotype.Component
import java.lang.RuntimeException

@Component
class GetTaxonomyCategoryWithAncestors(
    val taxonomyRepository: TaxonomyRepository
) {

    operator fun invoke(code: String): TaxonomyCategoryWithAncestors =
        taxonomyRepository.findByCode(code)?.let { taxonomyCategory ->
            TaxonomyCategoryWithAncestors(
                codeValue = code,
                description = taxonomyCategory.description,
                ancestors = getAncestorCodes(taxonomyCategory)
            )
        } ?: throw RuntimeException()

    private fun getAncestorCodes(taxonomyCategory: TaxonomyCategory): Set<String> {
        val parent = taxonomyCategory.parentCode?.let { taxonomyRepository.findByCode(it) }
        return parent?.let { getAncestorCodes(parent).plus(parent.codeValue) } ?: mutableSetOf()
    }
}

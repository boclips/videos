package com.boclips.videos.service.application

import com.boclips.videos.service.domain.model.taxonomy.CategoryCode
import com.boclips.videos.service.domain.model.taxonomy.TaxonomyCategoryWithAncestors
import com.boclips.videos.service.domain.service.video.TaxonomyRepository
import org.springframework.stereotype.Component
import java.lang.RuntimeException

@Component
class GetTaxonomyCategoryWithAncestors(
    val taxonomyRepository: TaxonomyRepository
) {

    operator fun invoke(rawCode: String): TaxonomyCategoryWithAncestors {
        val code = CategoryCode(rawCode)
        val category = taxonomyRepository.findByCode(code)
        return category?.let {
            TaxonomyCategoryWithAncestors(
                codeValue = it.code,
                description = it.description,
                ancestors = it.resolveParentsCodes()
            )
        } ?: throw RuntimeException()
    }
}

package com.boclips.videos.service.domain.service.taxonomy

import com.boclips.videos.service.domain.model.taxonomy.Taxonomy
import com.boclips.videos.service.domain.model.taxonomy.TaxonomyCategory
import com.boclips.videos.service.domain.model.taxonomy.TaxonomyTree
import com.boclips.videos.service.domain.service.video.TaxonomyRepository
import org.springframework.stereotype.Service

class TaxonomyService(
    private val taxonomyRepository: TaxonomyRepository
) {

    fun getTaxonomyTree(): Taxonomy {
        val taxonomyCategories = taxonomyRepository.findAll()
        val roots = taxonomyCategories.filter { it.parentCode == null }

        return roots.map { root ->
            root.codeValue to buildTree(filterRelevant(taxonomyCategories, root.codeValue), root)
        }.toMap()
    }

    private fun buildTree(taxonomyCategories: List<TaxonomyCategory>, current: TaxonomyCategory): TaxonomyTree {
        val children = taxonomyCategories.filter { it.parentCode == current.codeValue }

        if (children.isNotEmpty()) {
            return TaxonomyTree(
                description = current.description,
                code = current.codeValue,
                children = children.map { child ->
                    child.codeValue to buildTree(
                        taxonomyCategories = filterRelevant(taxonomyCategories, child.codeValue),
                        current = child
                    )
                }.toMap()
            )
        }

        return TaxonomyTree(description = current.description, code = current.codeValue, children = emptyMap())
    }

    fun addTaxonomy(taxonomyCategory: TaxonomyCategory): TaxonomyCategory {
        return taxonomyRepository.create(taxonomyCategory = taxonomyCategory)
    }

    private fun filterRelevant(taxonomyCategories: List<TaxonomyCategory>, relevantCode: String) =
        taxonomyCategories.filter { it.codeValue.startsWith(relevantCode) }

}

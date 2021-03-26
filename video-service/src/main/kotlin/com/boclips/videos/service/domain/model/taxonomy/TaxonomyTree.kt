package com.boclips.videos.service.domain.model.taxonomy

data class TaxonomyTree(
    val description: String,
    val code: String?,
    val children: Map<String, TaxonomyTree> = emptyMap()
) {
    companion object {
        fun buildTaxonomies(taxonomyCategories: List<TaxonomyCategory>): Taxonomy {

            val roots = taxonomyCategories.filter { it.parentCode == null }
            val topLevelChildren = roots.map { root ->
                root.codeValue to buildTree(filterRelevant(taxonomyCategories, root.codeValue), root)
            }.toMap()

            return topLevelChildren
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

        private fun filterRelevant(taxonomyCategories: List<TaxonomyCategory>, relevantCode: String) =
            taxonomyCategories.filter { it.codeValue.startsWith(relevantCode) }
    }
}

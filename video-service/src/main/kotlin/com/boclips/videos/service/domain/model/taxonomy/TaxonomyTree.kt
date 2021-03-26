package com.boclips.videos.service.domain.model.taxonomy

data class TaxonomyTree(
    val description: String?,
    val code: String?,
    val children: Map<String, TaxonomyTree> = emptyMap()
) {
    companion object {
        fun buildTaxonomies(taxonomies: List<Taxonomy>): TaxonomyCategories {

            val roots = taxonomies.filter { it.parentCode == null }
            val topLevelChildren = roots.map { root ->
                root.codeValue to buildTree(filterRelevant(taxonomies, root.codeValue), root)
            }.toMap()

            return topLevelChildren
        }

        private fun buildTree(taxonomies: List<Taxonomy>, current: Taxonomy): TaxonomyTree {
            val children = taxonomies.filter { it.parentCode == current.codeValue }

            if (children.isNotEmpty()) {
                return TaxonomyTree(
                    description = current.description,
                    code = current.codeValue,
                    children = children.map { child ->
                        child.codeValue to buildTree(
                            taxonomies = filterRelevant(taxonomies, child.codeValue),
                            current = child
                        )
                    }.toMap()
                )
            }

            return TaxonomyTree(description = current.description, code = current.codeValue, children = emptyMap())
        }

        private fun filterRelevant(taxonomies: List<Taxonomy>, relevantCode: String) =
            taxonomies.filter { it.codeValue.startsWith(relevantCode) }
    }
}

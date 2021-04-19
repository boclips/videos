package com.boclips.videos.service.infrastructure.video.converters

import com.boclips.videos.service.domain.model.taxonomy.Category
import com.boclips.videos.service.domain.model.taxonomy.CategoryCode
import com.boclips.videos.service.domain.model.taxonomy.TaxonomyCategory
import com.boclips.videos.service.infrastructure.taxonomy.TaxonomyNodeDocument
import org.bson.types.ObjectId

object TaxonomyDocumentConverter {
    fun toDocument(category: Category): TaxonomyNodeDocument {

        return TaxonomyNodeDocument(
            id = ObjectId(),
            codeValue = category.code.value,
            codeDescription = category.description,
            codeParent = category.parentCode?.value
        )
    }


    // TODO - to remove?
    fun toTaxonomy(nodeDocument: TaxonomyNodeDocument): TaxonomyCategory {
        return TaxonomyCategory(
            codeValue = nodeDocument.codeValue,
            description = nodeDocument.codeDescription,
            parentCode = nodeDocument.codeParent
        )
    }

    fun toCategories(nodeDocuments: List<TaxonomyNodeDocument>): List<Category> {
        val roots = nodeDocuments.filter { it.codeParent == null }
        return roots.map { root -> buildTree(filterRelevant(nodeDocuments, root.codeValue), root) }
    }

    private fun buildTree(nodeDocuments: List<TaxonomyNodeDocument>, current: TaxonomyNodeDocument): Category {
        val children = nodeDocuments.filter { it.codeParent == current.codeValue }

        if (children.isNotEmpty()) {
            return Category(
                description = current.codeDescription,
                code = CategoryCode(current.codeValue),
                children = children.map { child ->
                    CategoryCode(child.codeValue) to buildTree(
                        nodeDocuments = filterRelevant(nodeDocuments, child.codeValue),
                        current = child
                    )
                }.toMap(),
                parentCode = current.codeParent?.let { CategoryCode(it) }
            )
        }

        return Category(
            description = current.codeDescription,
            code = CategoryCode(current.codeValue),
            children = emptyMap(),
            parentCode = current.codeParent?.let { CategoryCode(it) }
        )
    }

    private fun filterRelevant(taxonomyNodeCategories: List<TaxonomyNodeDocument>, relevantCode: String) =
        taxonomyNodeCategories.filter { it.codeValue.startsWith(relevantCode) }

}

package com.boclips.videos.service.infrastructure.taxonomy

import com.boclips.videos.service.domain.model.taxonomy.Category
import com.boclips.videos.service.domain.model.taxonomy.CategoryCode
import org.bson.types.ObjectId

object CategoryDocumentConverter {
    fun toDocument(category: Category): CategoryDocument {
        return CategoryDocument(
            id = ObjectId(),
            codeValue = category.code.value,
            codeDescription = category.description,
            codeParent = category.parentCode?.value
        )
    }

    fun toCategory(nodeDocument: CategoryDocument): Category {
        return Category(
            code = CategoryCode(nodeDocument.codeValue),
            description = nodeDocument.codeDescription,
            parentCode = nodeDocument.codeParent?.let { CategoryCode(it) }
        )
    }
}

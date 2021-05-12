package com.boclips.videos.service.presentation.converters

import com.boclips.videos.api.response.channel.TaxonomyCategoryResource
import com.boclips.videos.service.domain.model.taxonomy.CategoryCode
import com.boclips.videos.service.domain.model.taxonomy.CategorySource
import com.boclips.videos.service.domain.model.taxonomy.CategoryWithAncestors

object VideoCategoryResourceConverter {
    fun toResource(categories: Map<CategorySource, Set<CategoryWithAncestors>>): List<TaxonomyCategoryResource>? {
        val values: MutableList<CategoryWithAncestors> = mutableListOf()

        categories.keys.forEach {
            categories.get(it)?.toList()?.forEach {
                values.add(it)
            }
        }

        return values.map {
            TaxonomyCategoryResource(
                codeValue = it.codeValue.value,
                description = it.description
            )
        }
    }

    fun toDocument(categoryResource: List<TaxonomyCategoryResource>?): Set<CategoryWithAncestors> {
        val values = categoryResource?.map {
            CategoryWithAncestors(
                codeValue = CategoryCode(it.codeValue),
                description = it.description
            )
        }

        return values?.toSet() ?: setOf(CategoryWithAncestors(codeValue = CategoryCode(""), description = ""))
    }
}

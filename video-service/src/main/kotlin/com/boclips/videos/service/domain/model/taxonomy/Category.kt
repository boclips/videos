package com.boclips.videos.service.domain.model.taxonomy

data class CategoryCode(val value: String)

data class Category(
    val parentCode: CategoryCode? = null,
    val description: String,
    val code: CategoryCode
) {

    fun resolveAncestorsCodes(): Set<CategoryCode> =
        IntRange(1, code.value.length - 1).asSequence()
            .map { code.value.take(it) }
            .map { CategoryCode(it) }
            .toSet()
}

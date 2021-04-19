package com.boclips.videos.service.domain.model.taxonomy

data class CategoryCode(val value: String)

data class Category(
    val parentCode: CategoryCode?,
    val description: String,
    val code: CategoryCode
) {

    fun resolveParentsCodes(): List<CategoryCode> {
        return code.value.toCharArray().map {
            CategoryCode(it.toString())
        }
    }
}

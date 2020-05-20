package com.boclips.videos.api.response.channel


import org.springframework.hateoas.server.core.Relation

@Relation(collectionRelation = "contentCategories")
open class ContentCategoriesResource(
    var _embedded: ContentCategoriesWrapperResource
 )

data class ContentCategoriesWrapperResource(
    val contentCategories:  List<ContentCategoryResource>
)

data class ContentCategoryResource(
    val key: String,
    val label: String
)

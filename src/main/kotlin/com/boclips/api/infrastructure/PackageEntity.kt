package com.boclips.api.infrastructure

import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import javax.persistence.Id

@Document(collection = "packages")
data class PackageEntity(
        @Id
        val id: String,
        val name: String,
        @Field("search_filters")
        val searchFilters: List<SearchFilter>
) {
    fun toPackage() = com.boclips.api.Package(
            id = this.id,
            name = this.name,
            excludedContentProviders = this.searchFilters
                    .filter { it._refType == SearchFilterType.Source }
                    .filter { it.invertFilter }
                    .flatMap { it.items }
    )
}

data class SearchFilter(
        val _refType: SearchFilterType,
        @Field("invert_filter")
        val invertFilter: Boolean,
        val items: List<String>
)

enum class SearchFilterType {
    Source, Assettype
}
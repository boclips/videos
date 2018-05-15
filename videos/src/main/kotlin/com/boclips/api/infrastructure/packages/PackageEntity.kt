package com.boclips.api.infrastructure.packages

import com.boclips.api.domain.model.ContentProvider
import com.boclips.api.domain.model.Package
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import javax.persistence.Id

@Document(collection = "packages")
data class PackageEntity(
        @Id
        val id: String,
        val name: String,
        @Field("search_filters")
        val searchFilters: MutableList<SearchFilter>
) {
    fun toPackage() = Package(
            id = this.id,
            name = this.name,
            excludedContentProviders = this.searchFilters
                    .filter { it._refType == SearchFilterType.Source }
                    .filter { it.invertFilter }
                    .flatMap { it.items.map { ContentProvider("", id = it) } }
    )
}

data class SearchFilter(
        val _refType: SearchFilterType,
        @Field("invert_filter")
        val invertFilter: Boolean,
        val items: MutableSet<String>
)

enum class SearchFilterType {
    Source, Assettype
}
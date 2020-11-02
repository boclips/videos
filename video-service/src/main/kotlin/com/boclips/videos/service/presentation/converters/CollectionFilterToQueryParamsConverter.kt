package com.boclips.videos.service.presentation.converters

import com.boclips.videos.api.request.collection.CollectionFilterRequest

class CollectionFilterToQueryParamsConverter {
    companion object {
        fun convert(collectionFilter: CollectionFilterRequest): Map<String, List<String>> {
            return listOfNotNull(
                collectionFilter.query?.let { "query" to listOf(it) },
                collectionFilter.discoverable?.let { "discoverable" to listOf(it.toString()) },
                collectionFilter.ignore_discoverable?.let { "ignore_discoverable" to listOf(it.toString()) },
                collectionFilter.bookmarked?.let { "bookmarked" to listOf(it.toString()) },
                collectionFilter.has_lesson_plans?.let { "has_lesson_plans" to listOf(it.toString()) },
                collectionFilter.promoted?.let { "promoted" to listOf(it.toString()) },
                collectionFilter.owner?.let { "owner" to listOf(it) },
                collectionFilter.subject?.let { "subject" to listOf(it) },
                collectionFilter.page?.let { "page" to listOf(it.toString()) },
                collectionFilter.size?.let { "size" to listOf(it.toString()) },
                collectionFilter.age_range_min?.let { "age_range_min" to listOf(it.toString()) },
                collectionFilter.age_range_max?.let { "age_range_max" to listOf(it.toString()) },
                collectionFilter.age_range?.let { "age_range" to collectionFilter.getAgeRanges() },
                collectionFilter.sort_by?.let { "sort_by" to it.split(",") },
                collectionFilter.resource_types?.let {"resource_types" to collectionFilter.getResourceTypes().toList() },
                collectionFilter.projection?.let {"projection" to listOf(it.toString())},
            ).toMap()
        }
    }
}

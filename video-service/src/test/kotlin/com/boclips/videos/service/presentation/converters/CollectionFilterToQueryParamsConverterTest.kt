package com.boclips.videos.service.presentation.converters

import com.boclips.videos.api.request.collection.CollectionFilterRequest
import com.boclips.videos.api.request.collection.CollectionSortKey
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

class CollectionFilterToQueryParamsConverterTest {

    @Test
    fun `it converts all parameters`() {
        val collectionFilterRequest = CollectionFilterRequest(
            query = "cats",
            discoverable = true,
            ignore_discoverable = false,
            bookmarked = true,
            promoted = true,
            has_lesson_plans = true,
            owner = "egyptians",
            subject = "miceKeeping",
            page = 1,
            size = 10,
            age_range_min = 2,
            age_range_max = 3,
            age_range = "02-03",
            sort_by = CollectionSortKey.TITLE.toString(),
            resource_types = "rescueCenter,street"
        )

        val filterMap = CollectionFilterToQueryParamsConverter.convert(collectionFilterRequest)

        Assertions.assertThat(filterMap.size).isEqualTo(16)
        Assertions.assertThat(filterMap["query"]).containsExactly("cats")
        Assertions.assertThat(filterMap["discoverable"]).containsExactly("true")
        Assertions.assertThat(filterMap["ignore_discoverable"]).containsExactly("false")
        Assertions.assertThat(filterMap["bookmarked"]).containsExactly("true")
        Assertions.assertThat(filterMap["promoted"]).containsExactly("true")
        Assertions.assertThat(filterMap["has_lesson_plans"]).containsExactly("true")
        Assertions.assertThat(filterMap["owner"]).containsExactly("egyptians")
        Assertions.assertThat(filterMap["subject"]).containsExactly("miceKeeping")
        Assertions.assertThat(filterMap["page"]).containsExactly("1")
        Assertions.assertThat(filterMap["size"]).containsExactly("10")
        Assertions.assertThat(filterMap["age_range_min"]).containsExactly("2")
        Assertions.assertThat(filterMap["age_range_max"]).containsExactly("3")
        Assertions.assertThat(filterMap["age_range"]).containsExactly("02-03")
        Assertions.assertThat(filterMap["sort_by"]).containsExactly("TITLE")
        Assertions.assertThat(filterMap["resource_types"]).containsExactly("rescueCenter", "street")
        Assertions.assertThat(filterMap["projection"]).containsExactly("list")
    }

    @Test
    fun `converts partial object`() {
        val collectionFilterRequest = CollectionFilterRequest(
            has_lesson_plans = true,
            owner = "egyptians",
        )

        val filterMap = CollectionFilterToQueryParamsConverter.convert(collectionFilterRequest)

        Assertions.assertThat(filterMap.size).isEqualTo(4)
        Assertions.assertThat(filterMap["has_lesson_plans"]).containsExactly("true")
        Assertions.assertThat(filterMap["owner"]).containsExactly("egyptians")
        Assertions.assertThat(filterMap["projection"]).containsExactly("list")
        Assertions.assertThat(filterMap["ignore_discoverable"]).containsExactly("false")
    }
}

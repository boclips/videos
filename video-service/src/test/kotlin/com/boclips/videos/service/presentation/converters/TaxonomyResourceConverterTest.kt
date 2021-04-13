package com.boclips.videos.service.presentation.converters

import com.boclips.videos.service.domain.model.taxonomy.Categories
import com.boclips.videos.service.domain.model.taxonomy.Category
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class TaxonomyResourceConverterTest {

    @Test
    fun `converts a tree to a resource`() {
        val categories: Categories =
            mapOf(
                ("A" to Category(description = "single BLAH", children = emptyMap(), code = "A")),
                ("B" to Category(
                    description = "parent BLAH", code = "B", children = mapOf(
                        ("BA" to Category(description = "child BLAH", children = emptyMap(), code = "BA"))
                    )
                ))
            )

        val resource = TaxonomyResourceConverter.toResource(categories)

        assertThat(resource._embedded["A"]?.children).isEmpty()
        assertThat(resource._embedded["A"]?.description).isEqualTo("single BLAH")
        assertThat(resource._embedded["B"]!!.children["BA"]?.description).isEqualTo("child BLAH")
        assertThat(resource._embedded["B"]?.description).isEqualTo("parent BLAH")
    }
}

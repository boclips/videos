package com.boclips.videos.service.presentation.converters

import com.boclips.videos.service.testsupport.CategoryFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class CategoryResourceConverterTest {

    @Test
    fun `converts a tree to a resource`() {
        val categories = listOf(
            CategoryFactory.sample(code = "A", description = "single BLAH"),
            CategoryFactory.sample(code = "B", description = "parent BLAH"),
            CategoryFactory.sample(
                code = "BA",
                parentCode = "B",
                description = "child BLAH"
            ),
            CategoryFactory.sample(
                code = "BAA",
                parentCode = "BA",
                description = "grandchild BLAH"
            )
        )

        CategoryFactory.sample(code = "B")

        val resource = CategoryResourceConverter.toResource(categories)

        assertThat(resource._embedded).hasSize(2)
        assertThat(resource._embedded["A"]?.children).isEmpty()
        assertThat(resource._embedded["A"]?.description).isEqualTo("single BLAH")
        assertThat(resource._embedded["B"]?.description).isEqualTo("parent BLAH")
        assertThat(resource._embedded["B"]?.children).hasSize(1)
        assertThat(resource._embedded["B"]!!.children["BA"]?.description).isEqualTo("child BLAH")
        assertThat(resource._embedded["B"]!!.children["BA"]!!.children["BAA"]?.children).isEmpty()
        assertThat(resource._embedded["B"]!!.children["BA"]!!.children["BAA"]?.description).isEqualTo("grandchild BLAH")
    }
}

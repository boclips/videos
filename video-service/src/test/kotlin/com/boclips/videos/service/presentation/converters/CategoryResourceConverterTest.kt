package com.boclips.videos.service.presentation.converters

import com.boclips.videos.service.domain.model.taxonomy.CategoryCode
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.CategoryFactory
import com.boclips.videos.service.testsupport.CategoryWithAncestorsFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class CategoryResourceConverterTest : AbstractSpringIntegrationTest() {

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

        val resource = categoryResourceConverter.toResource(categories)

        assertThat(resource._embedded).hasSize(2)
        assertThat(resource._embedded["A"]?.children).isEmpty()
        assertThat(resource._embedded["A"]?.description).isEqualTo("single BLAH")
        assertThat(resource._embedded["B"]?.description).isEqualTo("parent BLAH")
        assertThat(resource._embedded["B"]?.children).hasSize(1)
        assertThat(resource._embedded["B"]!!.children["BA"]?.description).isEqualTo("child BLAH")
        assertThat(resource._embedded["B"]!!.children["BA"]!!.children["BAA"]?.children).isEmpty()
        assertThat(resource._embedded["B"]!!.children["BA"]!!.children["BAA"]?.description).isEqualTo("grandchild BLAH")
    }

    @Test
    fun `reverse builds category tree`() {
        val sampleCategories = listOf(
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


        val category = CategoryWithAncestorsFactory.sample(
            codeValue = "BAA",
            ancestors = setOf(CategoryCode("BA"), CategoryCode("B"), CategoryCode("A")),
            description = "grandchild BLAH"
        )

        val resource = categoryResourceConverter.reverseBuildTree(sampleCategories, category)

        assertThat(resource.code).isEqualTo("BAA")
        assertThat(resource.value).isEqualTo("grandchild BLAH")
        assertThat(resource.parent?.code).isEqualTo("BA")
        assertThat(resource.parent?.value).isEqualTo("child BLAH")
        assertThat(resource.parent?.parent?.code).isEqualTo("B")
        assertThat(resource.parent?.parent?.value).isEqualTo("parent BLAH")

    }
}

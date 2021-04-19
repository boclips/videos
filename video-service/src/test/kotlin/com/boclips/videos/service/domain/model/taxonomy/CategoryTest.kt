package com.boclips.videos.service.domain.model.taxonomy

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class CategoryTest {

    @Test
    fun `returns parents categories codes`() {
        val category = Category(
            code = CategoryCode("ABCDE"),
            parentCode = CategoryCode("ABCD"),
            description = "Just a funny description"
        )

        assertThat(category.resolveAncestorsCodes()).isEqualTo(
            setOf(
                CategoryCode("A"),
                CategoryCode("AB"),
                CategoryCode("ABC"),
                CategoryCode("ABCD")
            )
        )
    }

    @Test
    fun `returns empty array when no parent code`() {
        val category = Category(
            code = CategoryCode("A"),
            parentCode = null,
            description = "Just a funny description"
        )

        assertThat(category.resolveAncestorsCodes()).isEmpty()
    }
}

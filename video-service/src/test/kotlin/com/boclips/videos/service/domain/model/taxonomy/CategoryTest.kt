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

        assertThat(category.resolveParentsCodes()).isEqualTo(listOf("A", "AB", "ABC", "ABCD"))
    }
}

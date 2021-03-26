package com.boclips.videos.service.presentation.converters

import com.boclips.videos.service.domain.model.taxonomy.TaxonomyCategories
import com.boclips.videos.service.domain.model.taxonomy.TaxonomyTree
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class TaxonomyResourceConverterTest {

    @Test
    fun `converts a tree to a resource`() {
        val taxonomyCategories: TaxonomyCategories = listOf(("a" to TaxonomyTree()))
    }
}

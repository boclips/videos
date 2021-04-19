package com.boclips.videos.service.infrastructure.taxonomy

import com.boclips.videos.service.domain.model.taxonomy.Category
import com.boclips.videos.service.domain.model.taxonomy.CategoryCode
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class TaxonomyCategoryDocumentConverterTest {
    @Test
    fun `converts a root taxonomy to a document and back`() {
        val originalTaxonomy = Category(code = CategoryCode("ABC"), description = "FRENCH")

        val doc = CategoryDocumentConverter.toDocument(originalTaxonomy)
        val newTax = CategoryDocumentConverter.toCategory(doc)

        assertThat(newTax).isEqualTo(originalTaxonomy)
    }

    @Test
    fun `converts a sub taxonomy to a document and back`() {
        val originalTaxonomy = Category(
            code = CategoryCode("ABC"),
            description = "FRENCH",
            parentCode = CategoryCode("AB")
        )

        val doc = CategoryDocumentConverter.toDocument(originalTaxonomy)
        val newTax = CategoryDocumentConverter.toCategory(doc)

        assertThat(newTax).isEqualTo(originalTaxonomy)
    }
}

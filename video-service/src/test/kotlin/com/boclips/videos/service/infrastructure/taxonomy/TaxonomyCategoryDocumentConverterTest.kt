package com.boclips.videos.service.infrastructure.taxonomy

import com.boclips.videos.service.domain.model.taxonomy.TaxonomyCategory
import com.boclips.videos.service.infrastructure.video.converters.TaxonomyDocumentConverter
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class TaxonomyCategoryDocumentConverterTest {
    @Test
    fun `converts a root taxonomy to a document and back`() {
        val originalTaxonomy = TaxonomyCategory(codeValue = "ABC", description = "FRENCH")

        val doc = TaxonomyDocumentConverter.toTaxonomyDocument(originalTaxonomy)
        val newTax = TaxonomyDocumentConverter.toTaxonomy(doc)

        assertThat(newTax).isEqualTo(originalTaxonomy)
    }

    @Test
    fun `converts a sub taxonomy to a document and back`() {
        val originalTaxonomy = TaxonomyCategory(
            codeValue = "ABC",
            description = "FRENCH",
            parentCode = "AB"
        )

        val doc = TaxonomyDocumentConverter.toTaxonomyDocument(originalTaxonomy)
        val newTax = TaxonomyDocumentConverter.toTaxonomy(doc)

        assertThat(newTax).isEqualTo(originalTaxonomy)
    }
}

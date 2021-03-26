package com.boclips.videos.service.domain.model.taxonomy

import com.boclips.videos.service.testsupport.TaxonomyFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class TaxonomyTreeTest {

    @Test
    fun `creates a taxonomy tree from a list of taxonomies`() {
        val parentTaxonomy = TaxonomyFactory.sample(codeValue = "A", description = "Hello from the root")
        val childTaxonomy = TaxonomyFactory.sample(codeValue = "AB", parentCode = parentTaxonomy.codeValue)
        val grandChildTaxonomy = TaxonomyFactory.sample(
            codeValue = "ABC",
            parentCode = childTaxonomy.codeValue,
            description = "Hello from the child"
        )
        val anotherTopLevel = TaxonomyFactory.sample(codeValue = "B")
        val taxonomies = listOf(parentTaxonomy, childTaxonomy, grandChildTaxonomy, anotherTopLevel)

        val tree = TaxonomyTree.buildTaxonomies(taxonomies)

        assertThat(tree["A"]?.code).isEqualTo("A")
        assertThat(tree["A"]?.description).isEqualTo("Hello from the root")
        assertThat(tree["B"]?.code).isEqualTo("B")
        assertThat(tree["A"]!!.children["AB"]?.code).isEqualTo("AB")
        assertThat(tree["A"]!!.children["AB"]!!.children["ABC"]?.code).isEqualTo("ABC")
        assertThat(tree["A"]!!.children["AB"]!!.children["ABC"]?.description).isEqualTo("Hello from the child")
    }
}

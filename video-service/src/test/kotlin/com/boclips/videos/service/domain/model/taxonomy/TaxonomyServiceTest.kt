package com.boclips.videos.service.domain.model.taxonomy

import com.boclips.videos.service.domain.service.taxonomy.TaxonomyService
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.TaxonomyFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class TaxonomyServiceTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var taxonomyService: TaxonomyService

    @Test
    fun `can add a taxonomy`() {
        val newTaxonomy = addTaxonomy(
            TaxonomyFactory.sample(
                codeValue = "ABC",
                description = "My new taxonomy",
                parentCode = "AB"
            )
        )
        assertThat(newTaxonomy.codeValue).isEqualTo("ABC")
        assertThat(newTaxonomy.description).isEqualTo("My new taxonomy")
        assertThat(newTaxonomy.parentCode).isEqualTo("AB")
    }

    @Test
    fun `creates a taxonomy tree from a list of taxonomies`() {
        addTaxonomy(TaxonomyFactory.sample(codeValue = "A", description = "Hello from the root"))
        addTaxonomy(TaxonomyFactory.sample(codeValue = "AB", parentCode = "A"))
        addTaxonomy(
            TaxonomyFactory.sample(
                codeValue = "ABC",
                parentCode = "AB",
                description = "Hello from the child"
            )
        )

        addTaxonomy(TaxonomyFactory.sample(codeValue = "B"))

        val tree = taxonomyService.getCategories()

        assertThat(tree["A"]?.code).isEqualTo("A")
        assertThat(tree["A"]?.description).isEqualTo("Hello from the root")
        assertThat(tree["B"]?.code).isEqualTo("B")
        assertThat(tree["A"]!!.children["AB"]?.code).isEqualTo("AB")
        assertThat(tree["A"]!!.children["AB"]!!.children["ABC"]?.code).isEqualTo("ABC")
        assertThat(tree["A"]!!.children["AB"]!!.children["ABC"]?.description).isEqualTo("Hello from the child")
    }
}

package com.boclips.videos.service.infrastructure.taxonomy

import com.boclips.videos.service.domain.model.taxonomy.Category
import com.boclips.videos.service.domain.model.taxonomy.CategoryCode
import com.boclips.videos.service.domain.service.taxonomy.CategoryRepository
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class MongoTaxonomyCategoryRepositoryIntegrationTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var mongoTaxonomyRepository: CategoryRepository

    @Test
    fun `can create a taxonomy`() {
        val bigTax = Category(code = CategoryCode("AB"), description = "The big parent tax")

        val createdTax = mongoTaxonomyRepository.create(bigTax)

        assertThat(createdTax).isEqualTo(bigTax)
    }

    @Test
    fun `can retrieve taxonomies`() {
        val smallTax = Category(code = CategoryCode("ABC"), description = "The lil child tax", parentCode = CategoryCode("AB"))
        val bigTax = Category(code = CategoryCode("AB"), description = "The big parent tax")
        mongoTaxonomyRepository.create(bigTax)
        mongoTaxonomyRepository.create(smallTax)

        val taxonomies = mongoTaxonomyRepository.findAll()

        assertThat(taxonomies.size).isEqualTo(2)
        assertThat(taxonomies).containsExactlyInAnyOrder(bigTax, smallTax)
    }
}

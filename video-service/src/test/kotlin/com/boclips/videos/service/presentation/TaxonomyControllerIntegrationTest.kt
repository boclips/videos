package com.boclips.videos.service.presentation

import com.boclips.videos.service.domain.service.video.TaxonomyRepository
import com.boclips.videos.service.infrastructure.taxonomy.MongoTaxonomyRepository
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.TaxonomyFactory
import com.boclips.videos.service.testsupport.asBoclipsEmployee
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

internal class TaxonomyControllerIntegrationTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var taxonomyRepository: TaxonomyRepository

    @Test
    fun `returns all taxonomy categories as boclips employee`() {

        val parentTaxonomy = TaxonomyFactory.sample(codeValue = "A")
        val childTaxonomy = TaxonomyFactory.sample(codeValue = "AB", parentCode = parentTaxonomy.codeValue)
        val grandChildTaxonomy = TaxonomyFactory.sample(codeValue = "ABC", parentCode = childTaxonomy.codeValue)

        taxonomyRepository.create(parentTaxonomy)
        taxonomyRepository.create(childTaxonomy)
        taxonomyRepository.create(grandChildTaxonomy)



        mockMvc.perform(get("/v1/taxonomies").asBoclipsEmployee())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._embedded.A.description", equalTo(parentTaxonomy.description)))
            .andExpect(jsonPath("$._embedded.A.children.AB.description", equalTo(childTaxonomy.description)))
            .andExpect(jsonPath("$._embedded.A.children.AB.children.ABC.description", equalTo(grandChildTaxonomy.description)))
    }
}

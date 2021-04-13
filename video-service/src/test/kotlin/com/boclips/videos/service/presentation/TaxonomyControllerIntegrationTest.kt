package com.boclips.videos.service.presentation

import com.boclips.videos.service.domain.service.taxonomy.TaxonomyService
import com.boclips.videos.service.domain.service.video.TaxonomyRepository
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.TaxonomyFactory
import com.boclips.videos.service.testsupport.asBoclipsEmployee
import org.hamcrest.Matchers.equalTo
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
    lateinit var taxonomyService: TaxonomyService

    @Test
    fun `returns all taxonomy categories as boclips employee`() {

        val parentTaxonomy = TaxonomyFactory.sample(codeValue = "A", description = "the parent taxonomy")
        val childTaxonomy = TaxonomyFactory.sample(
            codeValue = "AB",
            description = "the child taxonomy",
            parentCode = parentTaxonomy.codeValue
        )
        val grandChildTaxonomy = TaxonomyFactory.sample(
            codeValue = "ABC",
            description = "the grandchild taxonomy",
            parentCode = childTaxonomy.codeValue
        )

        taxonomyService.addTaxonomy(parentTaxonomy)
        taxonomyService.addTaxonomy(childTaxonomy)
        taxonomyService.addTaxonomy(grandChildTaxonomy)

        mockMvc.perform(get("/v1/categories").asBoclipsEmployee())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._embedded.A.description", equalTo(parentTaxonomy.description)))
            .andExpect(jsonPath("$._embedded.A.children.AB.description", equalTo(childTaxonomy.description)))
            .andExpect(
                jsonPath(
                    "$._embedded.A.children.AB.children.ABC.description",
                    equalTo(grandChildTaxonomy.description)
                )
            )
    }

    // TODO - [#177566333] - remove this test when boclips-api-client uses the /v1/categories one
    @Test
    fun `returns all taxonomy categories as boclips employee when legacy endpoint is used`() {

        val parentTaxonomy = TaxonomyFactory.sample(codeValue = "A", description = "the parent taxonomy")
        val childTaxonomy = TaxonomyFactory.sample(
            codeValue = "AB",
            description = "the child taxonomy",
            parentCode = parentTaxonomy.codeValue
        )
        val grandChildTaxonomy = TaxonomyFactory.sample(
            codeValue = "ABC",
            description = "the grandchild taxonomy",
            parentCode = childTaxonomy.codeValue
        )

        taxonomyService.addTaxonomy(parentTaxonomy)
        taxonomyService.addTaxonomy(childTaxonomy)
        taxonomyService.addTaxonomy(grandChildTaxonomy)

        mockMvc.perform(get("/v1/taxonomies").asBoclipsEmployee())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._embedded.A.description", equalTo(parentTaxonomy.description)))
            .andExpect(jsonPath("$._embedded.A.children.AB.description", equalTo(childTaxonomy.description)))
            .andExpect(
                jsonPath(
                    "$._embedded.A.children.AB.children.ABC.description",
                    equalTo(grandChildTaxonomy.description)
                )
            )
    }
}

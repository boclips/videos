package com.boclips.videos.service.presentation

import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.CategoryFactory
import com.boclips.videos.service.testsupport.asBoclipsEmployee
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

internal class CategoryControllerIntegrationTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `returns all taxonomy categories as boclips employee`() {

        val parentTaxonomy = CategoryFactory.sample(code = "A", description = "the parent taxonomy")
        val childTaxonomy = CategoryFactory.sample(
            code = "AB",
            description = "the child taxonomy",
            parentCode = parentTaxonomy.code.value
        )
        val grandChildTaxonomy = CategoryFactory.sample(
            code = "ABC",
            description = "the grandchild taxonomy",
            parentCode = childTaxonomy.code.value
        )

        saveCategory(parentTaxonomy)
        saveCategory(childTaxonomy)
        saveCategory(grandChildTaxonomy)

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
}

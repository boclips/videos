package com.boclips.videos.service.presentation

import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.asBoclipsEmployee
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

internal class AttachmentTypeControllerIntegrationTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `can fetch all attachment types`() {
        mockMvc.perform(get("/v1/attachment-types").asBoclipsEmployee())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._embedded.attachmentTypes", hasSize<Any>(3)))
            .andExpect(jsonPath("$._embedded.attachmentTypes[0].name", equalTo("LESSON_PLAN")))
            .andExpect(jsonPath("$._embedded.attachmentTypes[0].label", equalTo("Lesson Guide")))
            .andExpect(jsonPath("$._embedded.attachmentTypes[1].name", equalTo("FINAL_PROJECT")))
            .andExpect(jsonPath("$._embedded.attachmentTypes[1].label", equalTo("Final Project")))
            .andExpect(jsonPath("$._embedded.attachmentTypes[2].name", equalTo("ACTIVITY")))
            .andExpect(jsonPath("$._embedded.attachmentTypes[2].label", equalTo("Activity")))
    }
}

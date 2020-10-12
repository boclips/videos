package com.boclips.videos.service.presentation

import com.boclips.users.api.factories.AccessRulesResourceFactory
import com.boclips.users.api.response.accessrule.AccessRuleResource
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.asApiUser
import com.boclips.videos.service.testsupport.asTeacher
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath

class SuggestionsControllerIntegrationTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `provides suggestions for channels and subjects`() {
        val historyChannel = saveChannel(name = "The History Channel")
        saveChannel(name = "TED-Ed")
        val weLoveHistoryChannel = saveChannel(name = "We Love History")

        val subjectOne = saveSubject(name = "History")
        val subjectTwo = saveSubject(name = "Art History")
        saveSubject(name = "Mathematics")

        mockMvc.perform(MockMvcRequestBuilders.get("/v1/suggestions?query=his").asTeacher())
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(jsonPath("$.suggestionTerm", equalTo("his")))
            .andExpect(jsonPath("$.channels", hasSize<String>(2)))
            .andExpect(jsonPath("$.subjects", hasSize<String>(2)))
            .andExpect(
                jsonPath(
                    "$.channels[0].name",
                    equalTo("The History Channel")
                )
            )
            .andExpect(
                jsonPath(
                    "$.channels[0].id",
                    equalTo(historyChannel.id.value)
                )
            )
            .andExpect(
                jsonPath("$.channels[0]._links.searchVideos")
                    .exists()
            )
            .andExpect(
                jsonPath(
                    "$.channels[1].name",
                    equalTo("We Love History")
                )
            )
            .andExpect(
                jsonPath(
                    "$.channels[1].id",
                    equalTo(weLoveHistoryChannel.id.value)
                )
            )
            .andExpect(
                jsonPath(
                    "$.subjects[0].name",
                    equalTo(subjectOne.name)
                )
            )
            .andExpect(
                jsonPath(
                    "$.subjects[0].id",
                    equalTo(subjectOne.id.value)
                )
            )
            .andExpect(
                jsonPath("$.subjects[0]._links.searchVideos")
                    .exists()
            )
            .andExpect(
                jsonPath(
                    "$.subjects[1].name",
                    equalTo(subjectTwo.name)
                )
            )
            .andExpect(
                jsonPath(
                    "$.subjects[1].id",
                    equalTo(subjectTwo.id.value)
                )
            )
    }

    @Test
    fun `provides suggestions for channels without access rules`() {
        saveChannel(name = "The History Channel")
        val channel2 = saveChannel(name = "TED-Ed")
        saveChannel(name = "We Love History")
        saveChannel(name = "YMH")

        mockMvc.perform(MockMvcRequestBuilders.get("/v1/new-suggestions?query=ted").asApiUser(email = "api-user@gmail.com"))
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(jsonPath("$.channels", hasSize<Int>(1)))
            .andExpect(jsonPath("$.channels[0].id", equalTo(channel2.id.value)))
            .andExpect(jsonPath("$.channels[0].name", equalTo(channel2.name)))
    }
}

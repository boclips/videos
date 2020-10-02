package com.boclips.videos.service.presentation

import com.boclips.users.api.factories.AccessRulesResourceFactory
import com.boclips.users.api.response.accessrule.AccessRuleResource
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.asApiUser
import com.boclips.videos.service.testsupport.asTeacher
import org.hamcrest.Matchers
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

class SuggestionsControllerIntegrationTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `provides suggestions for channels and subjects`() {
        saveChannel(name = "The History Channel")
        saveChannel(name = "TED-Ed")
        saveChannel(name = "We Love History")

        val subjectOne = saveSubject(name = "History")
        val subjectTwo = saveSubject(name = "Art History")
        saveSubject(name = "Mathematics")

        mockMvc.perform(MockMvcRequestBuilders.get("/v1/suggestions?query=his").asTeacher())
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$.suggestionTerm", equalTo("his")))
            .andExpect(MockMvcResultMatchers.jsonPath("$.channels", hasSize<String>(2)))
            .andExpect(MockMvcResultMatchers.jsonPath("$.subjects", hasSize<String>(2)))
            .andExpect(
                MockMvcResultMatchers.jsonPath(
                    "$.channels[0].name",
                    equalTo("The History Channel")
                )
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.channels[0]._links.searchVideos")
                    .exists()
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath(
                    "$.channels[1].name",
                    equalTo("We Love History")
                )
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath(
                    "$.subjects[0].name",
                    equalTo(subjectOne.name)
                )
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath(
                    "$.subjects[0].id",
                    equalTo(subjectOne.id.value)
                )
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.subjects[0]._links.searchVideos")
                    .exists()
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath(
                    "$.subjects[1].name",
                    equalTo(subjectTwo.name)
                )
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath(
                    "$.subjects[1].id",
                    equalTo(subjectTwo.id.value)
                )
            )
    }

    @Test
    fun `provides suggestions based on access rules`() {
        saveChannel(name = "super channel")
        saveChannel(name = "super extra channel")
        val excludedChannel = saveChannel(name = "bad channel")

        usersClient.addAccessRules(
            "api-user@gmail.com",
            AccessRulesResourceFactory.sample(
                AccessRuleResource.ExcludedChannels(
                    id = "super bad channel",
                    name = "bad channel",
                    channelIds = listOf(excludedChannel.id.toString())
                )
            )
        )

        mockMvc.perform(MockMvcRequestBuilders.get("/v1/new-suggestions?query=cha").asApiUser())
            .andExpect(MockMvcResultMatchers.status().isOk)
    }
}

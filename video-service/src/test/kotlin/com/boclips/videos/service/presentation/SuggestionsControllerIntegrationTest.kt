package com.boclips.videos.service.presentation

import com.boclips.users.api.factories.AccessRulesResourceFactory
import com.boclips.users.api.response.accessrule.AccessRuleResource
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.asApiUser
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
    fun `provides suggestions for channels without access rules`() {
        saveChannel(name = "The History Channel")
        val channel2 = saveChannel(name = "TED-Ed")
        saveChannel(name = "We Love History")
        saveChannel(name = "YMH")

        mockMvc.perform(MockMvcRequestBuilders.get("/v1/suggestions?query=ted").asApiUser(email = "api-user@gmail.com"))
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(jsonPath("$.channels", hasSize<Int>(1)))
            .andExpect(jsonPath("$.channels[0].id", equalTo(channel2.id.value)))
            .andExpect(jsonPath("$.channels[0].name", equalTo(channel2.name)))
    }

    @Test
    fun `provides suggestions for subjects without access rules`() {
        saveChannel(name = "The History Channel")
        val channel = saveChannel(name = "Mathematics")
        saveChannel(name = "We Love History")
        saveChannel(name = "YMH")

        val subject = saveSubject(name = "Math")

        mockMvc.perform(MockMvcRequestBuilders.get("/v1/suggestions?query=math").asApiUser(email = "api-user@gmail.com"))
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(jsonPath("$.channels", hasSize<Int>(1)))
            .andExpect(jsonPath("$.channels[0].id", equalTo(channel.id.value)))
            .andExpect(jsonPath("$.channels[0].name", equalTo(channel.name)))
            .andExpect(jsonPath("$.subjects", hasSize<Int>(1)))
            .andExpect(jsonPath("$.subjects[0].id", equalTo(subject.id.value)))
            .andExpect(jsonPath("$.subjects[0].name", equalTo(subject.name)))
    }

    @Test
    fun `provides suggestions for channels with access rules`() {
        val channel1 = saveChannel(name = "The History Channel")
        val channel2 = saveChannel(name = "TED-Ed")
        saveChannel(name = "We Love History")

        usersClient.addAccessRules(
            "api-user@gmail.com",
            AccessRulesResourceFactory.sample(
                AccessRuleResource.IncludedChannels(
                    id = "include channels",
                    name = "channels",
                    channelIds = listOf(channel1.id.value, channel2.id.value)
                )
            )
        )

        mockMvc.perform(MockMvcRequestBuilders.get("/v1/suggestions?query=history").asApiUser(email = "api-user@gmail.com"))
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(jsonPath("$.channels", hasSize<Int>(1)))
    }
}

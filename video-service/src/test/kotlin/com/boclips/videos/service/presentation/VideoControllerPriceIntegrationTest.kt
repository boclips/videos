package com.boclips.videos.service.presentation

import com.boclips.users.api.factories.OrganisationResourceFactory
import com.boclips.users.api.factories.UserResourceFactory
import com.boclips.users.api.httpclient.test.fakes.OrganisationsClientFake
import com.boclips.users.api.httpclient.test.fakes.UsersClientFake
import com.boclips.users.api.response.organisation.DealResource
import com.boclips.users.api.response.organisation.OrganisationDetailsResource
import com.boclips.videos.service.config.security.UserRoles
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.asUserWithRoles
import com.boclips.videos.service.testsupport.asUserWithUsernameAndRoles
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class VideoControllerPriceIntegrationTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var usersClientFake: UsersClientFake

    @Autowired
    lateinit var organisationsClientFake: OrganisationsClientFake

    @Test
    fun `can retrieve price of a video`() {
        val videoId = saveVideo()

        mockMvc.perform(
            MockMvcRequestBuilders.get("/v1/videos/${videoId.value}")
                .asUserWithRoles(UserRoles.VIEW_VIDEOS, UserRoles.PUBLISHER)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.price.amount", equalTo(600)))
            .andExpect(jsonPath("$.price.currency", equalTo("USD")))
    }

    @Test
    fun `can retrieve user's organisation based price of a video`() {
        val videoId = saveVideo()

        organisationsClientFake.add(
            OrganisationResourceFactory.sample(
                id = "pearson-org",
                billing = true,
                deal = DealResource(
                    prices = DealResource.PricesResource(
                        videoTypePrices = mapOf(
                            "INSTRUCTIONAL" to DealResource.PriceResource("1000", "USD")
                        )
                    ),
                    accessExpiresOn = null,
                    billing = false,
                    contentPackageId = null
                ),
            )
        )

        usersClientFake.add(
            UserResourceFactory.sample(
                id = "a-pearson-user",
                organisation = OrganisationDetailsResource(
                    id = "pearson-org",
                    name = "Pearson",
                    domain = null,
                    type = null,
                    state = null,
                    country = null,
                    allowsOverridingUserIds = null,
                    features = null
                )
            )
        )

        mockMvc.perform(
            MockMvcRequestBuilders.get("/v1/videos/${videoId.value}")
                .asUserWithUsernameAndRoles("a-pearson-user", UserRoles.VIEW_VIDEOS, UserRoles.PUBLISHER)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.price.amount", equalTo(1000)))
            .andExpect(jsonPath("$.price.currency", equalTo("USD")))
    }

    @Test
    fun `can see user's organisation based prices when searching for videos`() {
        saveVideo()

        organisationsClientFake.add(
            OrganisationResourceFactory.sample(
                id = "pearson-org",
                billing = true,
                deal = DealResource(
                    prices = DealResource.PricesResource(
                        videoTypePrices = mapOf(
                            "INSTRUCTIONAL" to DealResource.PriceResource("1000", "USD")
                        )
                    ),
                    accessExpiresOn = null,
                    billing = false,
                    contentPackageId = null
                ),
            )
        )

        usersClientFake.add(
            UserResourceFactory.sample(
                id = "a-pearson-user",
                organisation = OrganisationDetailsResource(
                    id = "pearson-org",
                    name = "Pearson",
                    domain = null,
                    type = null,
                    state = null,
                    country = null,
                    allowsOverridingUserIds = null,
                    features = null
                )
            )
        )

        mockMvc.perform(
            MockMvcRequestBuilders.get("/v1/videos")
                .asUserWithUsernameAndRoles("a-pearson-user", UserRoles.VIEW_VIDEOS, UserRoles.PUBLISHER)
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._embedded.videos[0].price.amount", equalTo(1000)))
            .andExpect(jsonPath("$._embedded.videos[0].price.currency", equalTo("USD")))
    }

    @Test
    fun `user with internal role has access to price`() {
        val videoId = saveVideo()

        mockMvc.perform(
            MockMvcRequestBuilders.get("/v1/videos/${videoId.value}")
                .asUserWithRoles(UserRoles.VIEW_VIDEOS, UserRoles.BOCLIPS_SERVICE)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.price").exists())
    }

    @Test
    fun `user with missing roles does not have access to price`() {
        val videoId = saveVideo()

        mockMvc.perform(
            MockMvcRequestBuilders.get("/v1/videos/${videoId.value}").asUserWithRoles(UserRoles.VIEW_VIDEOS)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.price").doesNotExist())
    }
}

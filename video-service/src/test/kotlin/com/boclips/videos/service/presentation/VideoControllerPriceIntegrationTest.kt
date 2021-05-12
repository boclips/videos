package com.boclips.videos.service.presentation

import com.boclips.users.api.factories.OrganisationResourceFactory
import com.boclips.users.api.factories.UserResourceFactory
import com.boclips.users.api.httpclient.test.fakes.OrganisationsClientFake
import com.boclips.users.api.httpclient.test.fakes.UsersClientFake
import com.boclips.users.api.response.feature.FeatureKeyResource
import com.boclips.users.api.response.organisation.DealResource
import com.boclips.videos.service.config.security.UserRoles
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.asUserWithRoles
import com.boclips.videos.service.testsupport.asUserWithUsernameAndRoles
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Nested
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
                .asUserWithRoles(UserRoles.VIEW_VIDEOS, UserRoles.BOCLIPS_WEB_APP)
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
                        ),
                        channelPrices = mapOf(
                            "channel-TED" to DealResource.PriceResource(
                                "400",
                                "USD"
                            )
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
                organisation = OrganisationResourceFactory.sampleDetails(
                    id = "pearson-org",
                    name = "Pearson",
                    domain = null,
                    type = null,
                    state = null,
                    country = null,
                    allowsOverridingUserIds = null,
                    features = emptyMap()
                )
            )
        )

        mockMvc.perform(
            MockMvcRequestBuilders.get("/v1/videos/${videoId.value}")
                .asUserWithUsernameAndRoles("a-pearson-user", UserRoles.VIEW_VIDEOS, UserRoles.BOCLIPS_WEB_APP)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.price.amount", equalTo(1000)))
            .andExpect(jsonPath("$.price.currency", equalTo("USD")))
    }

    @Test
    fun `can see user's organisation channel custom prices when searching for videos`() {
        val channelWithCustomPrices = saveChannel(name = "TeD")
        saveVideo(existingChannelId = channelWithCustomPrices.id.value)

        organisationsClientFake.add(
            OrganisationResourceFactory.sample(
                id = "pearson-org",
                billing = true,
                deal = DealResource(
                    prices = DealResource.PricesResource(
                        videoTypePrices = mapOf(
                            "INSTRUCTIONAL" to DealResource.PriceResource("1000", "USD")
                        ),
                        channelPrices = mapOf(
                            channelWithCustomPrices.id.value to DealResource.PriceResource(
                                "4000",
                                "USD"
                            )
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
                organisation = OrganisationResourceFactory.sampleDetails(
                    id = "pearson-org",
                    name = "Pearson",
                    domain = null,
                    type = null,
                    state = null,
                    country = null,
                    allowsOverridingUserIds = null,
                    features = emptyMap()
                )
            )
        )

        mockMvc.perform(
            MockMvcRequestBuilders.get("/v1/videos")
                .asUserWithUsernameAndRoles("a-pearson-user", UserRoles.VIEW_VIDEOS, UserRoles.BOCLIPS_WEB_APP)
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._embedded.videos[0].price.amount", equalTo(4000)))
            .andExpect(jsonPath("$._embedded.videos[0].price.currency", equalTo("USD")))
    }

    @Test
    fun `can see user's organisation custom type prices when searching for videos`() {
        saveVideo()

        organisationsClientFake.add(
            OrganisationResourceFactory.sample(
                id = "pearson-org",
                billing = true,
                deal = DealResource(
                    prices = DealResource.PricesResource(
                        videoTypePrices = mapOf(
                            "INSTRUCTIONAL" to DealResource.PriceResource("1000", "USD")
                        ),
                        channelPrices = emptyMap()
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
                organisation = OrganisationResourceFactory.sampleDetails(
                    id = "pearson-org",
                    name = "Pearson",
                    domain = null,
                    type = null,
                    state = null,
                    country = null,
                    allowsOverridingUserIds = null,
                    features = emptyMap()
                )
            )
        )

        mockMvc.perform(
            MockMvcRequestBuilders.get("/v1/videos")
                .asUserWithUsernameAndRoles("a-pearson-user", UserRoles.VIEW_VIDEOS, UserRoles.BOCLIPS_WEB_APP)
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._embedded.videos[0].price.amount", equalTo(1000)))
            .andExpect(jsonPath("$._embedded.videos[0].price.currency", equalTo("USD")))
    }


    @Nested
    inner class Access {
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

        @Test
        fun `user with hidden prices config option does not receive prices`() {
            val videoId = saveVideo()
            organisationsClientFake.add(
                OrganisationResourceFactory.sample(
                    id = "pearson-org",
                    organisationDetails = OrganisationResourceFactory.sampleDetails(
                        id = "pearson-org",
                        features = mapOf(FeatureKeyResource.BO_WEB_APP_PRICES.toString() to false)
                    )
                )
            )
            usersClientFake.add(
                UserResourceFactory.sample(
                    id = "a-no-price-user",
                    organisation = OrganisationResourceFactory.sampleDetails(
                        id = "pearson-org",
                    )
                )
            )
            mockMvc.perform(
                MockMvcRequestBuilders.get("/v1/videos/${videoId.value}")
                    .asUserWithUsernameAndRoles("a-no-price-user", UserRoles.VIEW_VIDEOS, UserRoles.BOCLIPS_WEB_APP)
            ).andExpect(status().isOk)
                .andExpect(jsonPath("$.price").doesNotExist())
        }

    }


    @Test
    fun `requests for custom prices are rejected for non service accounts`() {
        val videoId = saveVideo()

        mockMvc.perform(
            MockMvcRequestBuilders.get("/v1/videos/${videoId.value}/price?userId=a-pearson-user")
                .asUserWithRoles()
        )
            .andExpect(status().isForbidden)
    }

    @Test
    fun `can get the custom price of a video for a given user`() {
        val videoId = saveVideo()
        organisationsClientFake.add(
            OrganisationResourceFactory.sample(
                id = "pearson-org",
                billing = true,
                deal = DealResource(
                    prices = DealResource.PricesResource(
                        videoTypePrices = mapOf(
                            "INSTRUCTIONAL" to DealResource.PriceResource("1000", "USD")
                        ),
                        channelPrices = emptyMap()
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
                organisation = OrganisationResourceFactory.sampleDetails(
                    id = "pearson-org",
                    name = "Pearson",
                    domain = null,
                    type = null,
                    state = null,
                    country = null,
                    allowsOverridingUserIds = null,
                    features = emptyMap()
                )
            )
        )
        mockMvc.perform(
            MockMvcRequestBuilders.get("/v1/videos/${videoId.value}/price?userId=a-pearson-user")
                .asUserWithRoles(UserRoles.VIEW_VIDEOS)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.amount", equalTo(1000)))
            .andExpect(jsonPath("$.currency", equalTo("USD")))
    }
}

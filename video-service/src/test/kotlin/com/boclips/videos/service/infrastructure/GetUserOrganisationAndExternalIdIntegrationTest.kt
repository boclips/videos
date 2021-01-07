package com.boclips.videos.service.infrastructure

import com.boclips.users.api.factories.OrganisationResourceFactory
import com.boclips.users.api.factories.UserResourceFactory
import com.boclips.videos.service.domain.model.user.Deal
import com.boclips.videos.service.domain.model.user.Organisation
import com.boclips.videos.service.domain.model.user.OrganisationId
import com.boclips.videos.service.domain.model.user.UserId
import com.boclips.videos.service.infrastructure.user.GetUserOrganisationAndExternalId
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.SecurityUserFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes

class GetUserOrganisationAndExternalIdIntegrationTest : AbstractSpringIntegrationTest() {

    @Autowired
    private lateinit var getUserOrganisationAndOverrideUserId: GetUserOrganisationAndExternalId

    @Test
    fun `returns null if user is not found in user service`() {
        val userIdOverride = getUserOrganisationAndOverrideUserId(SecurityUserFactory.sample())

        assertThat(userIdOverride).isNull()
    }

    @Test
    fun `returns null if user does not have an organisation`() {
        usersClient.add(UserResourceFactory.sample("user-id"))
        organisationsClient.clear()

        val userIdOverride = getUserOrganisationAndOverrideUserId(SecurityUserFactory.sample(id = "user-id"))

        assertThat(userIdOverride).isNull()
    }

    @Test
    fun `returns organisation if user ID is not overridable`() {
        val organisationDetails = OrganisationResourceFactory.sampleDetails(
                id = "organisation-id"
        )
        usersClient.add(
            UserResourceFactory.sample(
                id = "user-id",
                organisation = organisationDetails
            )
        )
        organisationsClient.add(
            OrganisationResourceFactory.sample(
                id = "organisation-id",
                organisationDetails = organisationDetails,
                billing = null
            )
        )

        val userIdOverride = getUserOrganisationAndOverrideUserId(SecurityUserFactory.sample(id = "user-id"))!!

        assertThat(userIdOverride.first).isNull()
        assertThat(userIdOverride.second).isEqualTo(
                Organisation(
                        organisationId = OrganisationId(value="organisation-id"),
                        allowOverridingUserIds = false,
                        deal = Deal(
                                prices = Deal.Prices(
                                        videoTypePrices = emptyMap()
                                )
                        )
                )
        )
    }

    @Test
    fun `returns organisation and external user ID if user ID is overridable`() {
        val request = MockHttpServletRequest().also {
            it.addHeader("Boclips-User-Id", "external-id")
        }
        RequestContextHolder.setRequestAttributes(
                ServletRequestAttributes(
                        request
                )
        )
        val organisationDetails = OrganisationResourceFactory.sampleDetails(
                id = "organisation-id",
                allowsOverridingUserIds = true
        )
        usersClient.add(
            UserResourceFactory.sample(
                id = "user-id",
                organisation = organisationDetails
            )
        )
        organisationsClient.add(
            OrganisationResourceFactory.sample(
                id = "organisation-id",
                organisationDetails = organisationDetails,
                billing = null
            )
        )

        val userIdOverride = getUserOrganisationAndOverrideUserId(SecurityUserFactory.sample(id = "user-id"))!!

        assertThat(userIdOverride.first).isEqualTo(UserId("external-id"))
        assertThat(userIdOverride.second).isEqualTo(
                Organisation(
                        organisationId = OrganisationId(value="organisation-id"),
                        allowOverridingUserIds = true,
                        deal = Deal(
                                prices = Deal.Prices(
                                        videoTypePrices = emptyMap()
                                )
                        )
                )
        )
    }

    @Test
    fun `returns only organisation if user ID is overridable despite appropriate request header presence`() {
        val request = MockHttpServletRequest().also {
            it.addHeader("Boclips-User-Id", "external-id")
        }
        RequestContextHolder.setRequestAttributes(
                ServletRequestAttributes(
                        request
                )
        )
        val organisationDetails = OrganisationResourceFactory.sampleDetails(
                id = "organisation-id",
                allowsOverridingUserIds = false
        )
        usersClient.add(
            UserResourceFactory.sample(
                id = "user-id",
                organisation = organisationDetails
            )
        )
        organisationsClient.add(
            OrganisationResourceFactory.sample(
                id = "organisation-id",
                organisationDetails = organisationDetails,
                billing = null
            )
        )

        val userIdOverride = getUserOrganisationAndOverrideUserId(SecurityUserFactory.sample(id = "user-id"))!!

        assertThat(userIdOverride.first).isNull()
        assertThat(userIdOverride.second).isEqualTo(
                Organisation(
                        organisationId = OrganisationId(value="organisation-id"),
                        allowOverridingUserIds = false,
                        deal = Deal(
                                prices = Deal.Prices(
                                        videoTypePrices = emptyMap()
                                )
                        )
                )
        )
    }
}

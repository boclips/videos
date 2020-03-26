package com.boclips.videos.service.infrastructure

import com.boclips.users.api.factories.UserResourceFactory
import com.boclips.users.api.response.organisation.OrganisationDetailsResource
import com.boclips.users.api.response.organisation.OrganisationResource
import com.boclips.videos.service.domain.service.GetUserIdOverride
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.SecurityUserFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class ApiGetUserIdOverrideIntegrationTest : AbstractSpringIntegrationTest() {
    @Autowired
    private lateinit var getUserIdOverride: GetUserIdOverride

    @Test
    fun `it returns null if user is not found in user service`() {
        val userIdOverride = getUserIdOverride(SecurityUserFactory.sample())

        assertThat(userIdOverride).isNull()
    }

    @Test
    fun `it returns null if user is not does not have an organisation`() {
        usersClient.add(UserResourceFactory.sample("user-id"))
        organisationsClient.clear()

        val userIdOverride = getUserIdOverride(SecurityUserFactory.sample())

        assertThat(userIdOverride).isNull()
    }

    @Test
    fun `returns null if account does not have an organisation`() {
        usersClient.add(UserResourceFactory.sample(id = "user-id"))
        organisationsClient.add(
            OrganisationResource(
                id = "account-id",
                accessExpiresOn = null,
                contentPackageId = null,
                organisationDetails = OrganisationDetailsResource(
                    "organisation", null, null, null, null, null
                ),
                _links = null
            )
        )

        val userIdOverride = getUserIdOverride(SecurityUserFactory.sample())

        assertThat(userIdOverride).isNull()
    }
}

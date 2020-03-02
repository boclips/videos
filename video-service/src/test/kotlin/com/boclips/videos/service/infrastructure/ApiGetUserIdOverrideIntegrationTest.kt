package com.boclips.videos.service.infrastructure

import com.boclips.users.client.model.Organisation
import com.boclips.users.client.model.User
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
        userServiceClient.clearUser()

        val userIdOverride = getUserIdOverride(SecurityUserFactory.sample())

        assertThat(userIdOverride).isNull()
    }

    @Test
    fun `it returns null if user is not does not have an organisation`() {
        userServiceClient.addUser(User("user-id", null, emptyList(), null))
        userServiceClient.clearOrganisation()

        val userIdOverride = getUserIdOverride(SecurityUserFactory.sample())

        assertThat(userIdOverride).isNull()
    }

    @Test
    fun `returns null if account does not have an organisation`() {
        userServiceClient.addUser(User("user-id", null, emptyList(), null))
        userServiceClient.addOrganisation(Organisation("account-id", null))

        val userIdOverride = getUserIdOverride(SecurityUserFactory.sample())

        assertThat(userIdOverride).isNull()
    }
}

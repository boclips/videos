package com.boclips.videos.service.infrastructure.user

import com.boclips.users.client.model.Subject
import com.boclips.users.client.model.User
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.util.Arrays

class ApiUserServiceIntegrationTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var userService: ApiUserService

    @Test
    fun getSubjectIds() {
        userServiceClient.addUser(User("bob@boclips.com", "organisation-1", Arrays.asList(Subject("subject-1")), null))

        val subjectIds = userService.getSubjectIds("bob@boclips.com")

        assertThat(subjectIds).containsExactly("subject-1")
    }

    @Test
    fun `getSubjectIds when user not found`() {
        val subjectIds = userService.getSubjectIds("bob@boclips.com")

        assertThat(subjectIds).isNull()
    }

    @Test
    fun `getSubjectIds returns cached subjects`() {
        userServiceClient.addUser(User("bob@boclips.com", "organisation-1", Arrays.asList(Subject("subject-1")), null))
        userService.getSubjectIds("bob@boclips.com")
        userServiceClient.clearUser()

        val subjectIds = userService.getSubjectIds("bob@boclips.com")

        assertThat(subjectIds).containsExactly("subject-1")
    }

}

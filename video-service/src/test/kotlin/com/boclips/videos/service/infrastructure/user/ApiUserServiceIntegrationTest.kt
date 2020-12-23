package com.boclips.videos.service.infrastructure.user

import com.boclips.users.api.factories.OrganisationResourceFactory
import com.boclips.users.api.factories.UserResourceFactory
import com.boclips.users.api.response.SubjectResource
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class ApiUserServiceIntegrationTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var userService: ApiUserService

    @Test
    fun getSubjectIds() {
        usersClient.add(
            UserResourceFactory.sample(
                subjects = listOf(SubjectResource("subject-1")),
                organisation = OrganisationResourceFactory.sampleDetails(
                    id = "organisation-1"
                ),
                id = "bob@boclips.com"
            )
        )

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
        usersClient.add(
            UserResourceFactory.sample(
                id = "bob@boclips.com",
                organisation = OrganisationResourceFactory.sampleDetails(
                    id = "organisation-1"
                ),
                subjects = listOf(SubjectResource("subject-1"))
            )
        )

        userService.getSubjectIds("bob@boclips.com")
        usersClient.clear()

        val subjectIds = userService.getSubjectIds("bob@boclips.com")

        assertThat(subjectIds).containsExactly("subject-1")
    }

    @Test
    fun `can get an organisation of user`() {
        usersClient.add(
            UserResourceFactory.sample(
                id = "bob@boclips.com",
                organisation = OrganisationResourceFactory.sampleDetails(
                    id = "organisation-1"
                ),
                subjects = listOf(SubjectResource("subject-1"))
            )
        )

        organisationsClient.add(
            OrganisationResourceFactory.sample(
                id = "organisation-1",
                organisationDetails = OrganisationResourceFactory.sampleDetails(
                    name = "hello",
                    allowsOverridingUserIds = true
                ),
                billing = null
            )
        )

        val organisation = userService.getOrganisationOfUser("bob@boclips.com")!!
        assertThat(organisation.organisationId.value).isEqualTo("organisation-1")
        assertThat(organisation.allowOverridingUserIds).isTrue()
    }

    @Test
    fun `organisation is null when no user present`() {
        val organisation = userService.getOrganisationOfUser("bob@boclips.com")
        assertThat(organisation).isNull()
    }
}

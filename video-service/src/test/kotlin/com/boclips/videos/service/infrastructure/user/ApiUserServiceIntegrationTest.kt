package com.boclips.videos.service.infrastructure.user

import com.boclips.users.api.factories.UserResourceFactory
import com.boclips.users.api.response.SubjectResource
import com.boclips.users.api.response.organisation.OrganisationDetailsResource
import com.boclips.users.api.response.organisation.OrganisationResource
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
                organisationAccountId = "organisation-1",
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
                organisationAccountId = "organisation-1",
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
                organisationAccountId = "organisation-1",
                subjects = listOf(SubjectResource("subject-1"))

            )
        )

        organisationsClient.add(
            OrganisationResource(
                id = "organisation-1",
                accessExpiresOn = null,
                contentPackageId = null,
                organisationDetails = OrganisationDetailsResource(
                    name = "hello",
                    allowsOverridingUserIds = true,
                    country = null,
                    domain = null,
                    type = null,
                    state = null
                ),
                _links = null
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

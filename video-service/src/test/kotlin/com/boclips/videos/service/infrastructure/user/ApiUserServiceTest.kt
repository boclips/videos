package com.boclips.videos.service.infrastructure.user

import com.boclips.users.api.httpclient.OrganisationsClient
import com.boclips.users.api.httpclient.UsersClient
import com.boclips.users.api.httpclient.test.fakes.FakeClient
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ApiUserServiceTest {
    lateinit var usersClient: UsersClient
    lateinit var organisationsClient: OrganisationsClient
    lateinit var apiUserService: ApiUserService

    @BeforeEach
    fun setup() {
        usersClient = mock()
        organisationsClient = mock()
        apiUserService = ApiUserService(usersClient, organisationsClient)
    }

    @Test
    fun `getSubjectIds handles user service client exceptions`() {
        whenever(usersClient.getUser(any())).thenThrow(RuntimeException("Boooom!"))

        assertThat(apiUserService.getSubjectIds("bob@gmail.com")).isNull()
    }

    @Test
    fun `validate share codes is false on exception`() {

        whenever(usersClient.getShareCode(any(), any())).thenThrow(FakeClient.forbiddenException("Boooom!"))

        assertThat(apiUserService.isShareCodeValid("bob@gmail.com", "123")).isFalse()
    }
}

package com.boclips.videos.service.infrastructure.user

import com.boclips.users.client.UserServiceClient
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ApiUserServiceTest {

    @Test
    fun `getSubjectIds handles user service client exceptions`() {
        val userServiceClient = mock<UserServiceClient>()
        val apiUserService = ApiUserService(userServiceClient)

        whenever(userServiceClient.findUser(any())).thenThrow(RuntimeException("Boooom!"))

        assertThat(apiUserService.getSubjectIds("bob@gmail.com")).isNull()
    }
}

package com.boclips.videos.service.config

import com.boclips.users.api.httpclient.OrganisationsClient
import com.boclips.users.api.httpclient.UsersClient
import com.boclips.users.api.httpclient.helper.ServiceAccountCredentials
import com.boclips.users.api.httpclient.helper.ServiceAccountTokenFactory
import com.boclips.videos.service.config.properties.UserClientProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Profile("!fake-user-service")
@Configuration
class UserServiceContext {
    @Bean
    fun usersClient(userClientProperties: UserClientProperties) = UsersClient.create(
        apiUrl = userClientProperties.baseUrl,
        tokenFactory = ServiceAccountTokenFactory(
            serviceAccountCredentials = ServiceAccountCredentials(
                authEndpoint = userClientProperties.tokenUrl,
                clientId = userClientProperties.clientId,
                clientSecret = userClientProperties.clientSecret
            )
        )
    )

    @Bean
    fun organisationsClient(userClientProperties: UserClientProperties) = OrganisationsClient.create(
        apiUrl = userClientProperties.baseUrl,
        tokenFactory = ServiceAccountTokenFactory(
            serviceAccountCredentials = ServiceAccountCredentials(
                authEndpoint = userClientProperties.tokenUrl,
                clientId = userClientProperties.clientId,
                clientSecret = userClientProperties.clientSecret
            )
        )
    )
}

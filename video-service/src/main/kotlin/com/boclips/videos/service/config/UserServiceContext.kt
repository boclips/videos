package com.boclips.videos.service.config

import com.boclips.users.api.httpclient.ContentPackagesClient
import com.boclips.users.api.httpclient.OrganisationsClient
import com.boclips.users.api.httpclient.UsersClient
import com.boclips.users.api.httpclient.helper.ServiceAccountCredentials
import com.boclips.users.api.httpclient.helper.ServiceAccountTokenFactory
import com.boclips.videos.service.config.properties.UserClientProperties
import feign.okhttp.OkHttpClient
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Profile("!fake-user-service")
@Configuration
class UserServiceContext {

    @Bean
    fun usersFeignClient() = OkHttpClient()

    @Bean
    fun usersClient(
            userClientProperties: UserClientProperties,
            @Qualifier("usersFeignClient") usersFeignClient: OkHttpClient
    ) =
        UsersClient.create(
            apiUrl = userClientProperties.baseUrl,
            feignClient = usersFeignClient,
            tokenFactory = userClientProperties.tokenFactory()
        )

    @Bean
    fun organisationsFeignClient() = OkHttpClient()

    @Bean
    fun organisationsClient(
            userClientProperties: UserClientProperties,
            @Qualifier("organisationsFeignClient") organisationsFeignClient: OkHttpClient
    ) =
        OrganisationsClient.create(
            apiUrl = userClientProperties.baseUrl,
            feignClient = organisationsFeignClient,
            tokenFactory = userClientProperties.tokenFactory()
        )

    @Bean
    fun contentPackagesFeignClient() = OkHttpClient()

    @Bean
    fun contentPackagesClient(
            userClientProperties: UserClientProperties,
            @Qualifier("contentPackagesFeignClient") contentPackagesFeignClient: OkHttpClient
    ) =
        ContentPackagesClient.create(
            apiUrl = userClientProperties.baseUrl,
            feignClient = contentPackagesFeignClient,
            tokenFactory = userClientProperties.tokenFactory()
        )
}

fun UserClientProperties.tokenFactory() =
    ServiceAccountTokenFactory(
        serviceAccountCredentials = ServiceAccountCredentials(
            authEndpoint = tokenUrl,
            clientId = clientId,
            clientSecret = clientSecret
        )
    )

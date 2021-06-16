package com.boclips.videos.service.config

import com.boclips.users.api.httpclient.ContentPackagesClient
import com.boclips.users.api.httpclient.OrganisationsClient
import com.boclips.users.api.httpclient.UsersClient
import com.boclips.users.api.httpclient.helper.ServiceAccountCredentials
import com.boclips.users.api.httpclient.helper.ServiceAccountTokenFactory
import com.boclips.videos.service.config.properties.UserClientProperties
import feign.okhttp.OkHttpClient
import feign.opentracing.TracingClient
import io.opentracing.Tracer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Profile("!fake-user-service")
@Configuration
class UserServiceContext {

    @Bean
    fun usersClient(
        userClientProperties: UserClientProperties,
        tracer: Tracer
    ): UsersClient = UsersClient.create(
        apiUrl = userClientProperties.baseUrl,
        feignClient = createTracingClient(tracer),
        tokenFactory = userClientProperties.tokenFactory()
    )

    @Bean
    fun organisationsClient(
        userClientProperties: UserClientProperties,
        tracer: Tracer
    ) =
        OrganisationsClient.create(
            apiUrl = userClientProperties.baseUrl,
            feignClient = createTracingClient(tracer),
            tokenFactory = userClientProperties.tokenFactory()
        )

    @Bean
    fun contentPackagesClient(
        userClientProperties: UserClientProperties,
        tracer: Tracer
    ) =
        ContentPackagesClient.create(
            apiUrl = userClientProperties.baseUrl,
            feignClient = createTracingClient(tracer),
            tokenFactory = userClientProperties.tokenFactory()
        )

    private fun createTracingClient(tracer: Tracer): TracingClient {
        val delegate = OkHttpClient()
        return TracingClient(delegate, tracer)
    }
}

fun UserClientProperties.tokenFactory() =
    ServiceAccountTokenFactory(
        serviceAccountCredentials = ServiceAccountCredentials(
            authEndpoint = tokenUrl,
            clientId = clientId,
            clientSecret = clientSecret
        )
    )

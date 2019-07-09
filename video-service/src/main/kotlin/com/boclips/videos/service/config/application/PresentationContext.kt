package com.boclips.videos.service.config.application

import com.boclips.videos.service.presentation.projections.RoleBasedProjectionResolver
import com.boclips.videos.service.presentation.projections.WithProjection
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.filter.ForwardedHeaderFilter

@Configuration
class PresentationContext {

    @Bean
    fun forwardedHeaderFilter(): FilterRegistrationBean<ForwardedHeaderFilter> {
        val filter = FilterRegistrationBean<ForwardedHeaderFilter>()
        filter.filter = ForwardedHeaderFilter()
        return filter
    }

    @Bean
    fun projectionResolver() = RoleBasedProjectionResolver()

    @Bean
    fun withProjection() = WithProjection(projectionResolver())
}
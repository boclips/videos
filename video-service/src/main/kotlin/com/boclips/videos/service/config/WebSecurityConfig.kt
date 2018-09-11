package com.boclips.videos.service.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.userdetails.User
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.provisioning.InMemoryUserDetailsManager
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import java.util.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


@Configuration
@EnableWebSecurity
class WebSecurityConfig : WebSecurityConfigurerAdapter() {
    @Throws(Exception::class)
    override fun configure(http: HttpSecurity) {
        http
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
                .httpBasic().authenticationEntryPoint(authenticationEntryPoint()).and()
                .authorizeRequests()
                .antMatchers("/actuator/**").permitAll()
                .antMatchers("/v1").permitAll()
                .antMatchers("/v1/").permitAll()
                .anyRequest().authenticated()
    }

    @Bean
    public override fun userDetailsService() =
            InMemoryUserDetailsManager(
                    User.withUsername("teacher")
                            .password(passwordEncoder().encode("test"))
                            .roles("VIDEOS")
                            .build()
            )

    @Bean
    fun passwordEncoder() = BCryptPasswordEncoder()

    @Bean
    fun authenticationEntryPoint() = NoBrowserPromptBasicEntryPoint()

}

class NoBrowserPromptBasicEntryPoint : BasicAuthenticationEntryPoint() {

    override fun commence(request: HttpServletRequest, response: HttpServletResponse, authException: AuthenticationException?) {
        response.addHeader("WWW-Authenticate", "CustomForm")
        response.status = HttpServletResponse.SC_UNAUTHORIZED
    }

    override fun afterPropertiesSet() {
        realmName = "teacherspet"
        super.afterPropertiesSet()
    }
}
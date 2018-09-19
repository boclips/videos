package com.boclips.videos.service.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.userdetails.User
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.provisioning.InMemoryUserDetailsManager
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer


@Configuration
@EnableWebSecurity
class WebSecurityConfig(val boclipsProperties: BoclipsConfigProperties) : WebSecurityConfigurerAdapter() {
    @Throws(Exception::class)
    override fun configure(http: HttpSecurity) {
        http
                .csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
                .httpBasic().authenticationEntryPoint(authenticationEntryPoint()).and()
                .authorizeRequests()
                .antMatchers("/actuator/**").permitAll()
                .antMatchers("/v1").permitAll()
                .antMatchers("/v1/").permitAll()
                .antMatchers(HttpMethod.OPTIONS, "/v1/**").permitAll()
                .anyRequest().authenticated()
    }

    @Bean
    fun corsConfigurer(): WebMvcConfigurer {
        return object : WebMvcConfigurer {
            override fun addCorsMappings(registry: CorsRegistry) {
                registry.addMapping("/**").allowedOrigins("http://localhost:8080")
            }
        }
    }

    @Bean
    public override fun userDetailsService() =
            InMemoryUserDetailsManager(
                    User.withUsername(boclipsProperties.teacher.username
                            ?: throw IllegalStateException("Missing env-var BOCLIPS_TEACHER_USERNAME - used to log in"))
                            .password(passwordEncoder().encode(boclipsProperties.teacher.password)
                                    ?: throw IllegalStateException("Missing env-var BOCLIPS_TEACHER_PASSWORD - used to log in"))
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
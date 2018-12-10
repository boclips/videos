package com.boclips.videos.service.config

import com.boclips.security.testing.MockBoclipsSecurity
import com.boclips.videos.service.config.security.UserRoles.INSERT_VIDEOS
import org.springframework.context.annotation.Profile
import org.springframework.security.config.annotation.SecurityConfigurerAdapter
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter

@Profile("fake-security")
@MockBoclipsSecurity
class SecurityConfigFake
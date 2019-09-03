package com.boclips.videos.service.config

import com.boclips.users.client.spring.EnableUserServiceClient
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Profile("!test")
@Configuration
@EnableUserServiceClient
class UserServiceContext

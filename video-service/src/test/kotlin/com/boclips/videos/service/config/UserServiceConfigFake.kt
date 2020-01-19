package com.boclips.videos.service.config

import com.boclips.users.client.spring.MockUserServiceClient
import org.springframework.context.annotation.Configuration

@MockUserServiceClient
@Configuration
class UserServiceConfigFake {
}

package com.boclips.videos.service.config

import com.boclips.security.testing.MockBoclipsSecurity
import org.springframework.context.annotation.Profile

@Profile("fake-security")
@MockBoclipsSecurity
class SecurityConfigFake
package com.boclips.videos.service.config

import com.boclips.security.testing.MockBoclipsSecurity
import org.springframework.context.annotation.Profile

@Profile("fakes-security & !no-security")
@MockBoclipsSecurity
class SecurityConfigFake

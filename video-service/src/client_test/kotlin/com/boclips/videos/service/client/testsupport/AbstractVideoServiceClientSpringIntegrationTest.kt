package com.boclips.videos.service.client.testsupport

import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("no-security")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class AbstractVideoServiceClientSpringIntegrationTest : AbstractSpringIntegrationTest() {

}
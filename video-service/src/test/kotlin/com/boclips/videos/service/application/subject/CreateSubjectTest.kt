package com.boclips.videos.service.application.subject

import com.boclips.videos.service.application.exceptions.SubjectExistsException
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.TestFactories
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class CreateSubjectTest : AbstractSpringIntegrationTest() {
    @Test
    fun `throws when subject already exists`() {
        val createRequest = TestFactories.createSubjectRequest(name = "Music")

        createSubject(createRequest)

        assertThrows<SubjectExistsException> { createSubject(createRequest) }
    }
}

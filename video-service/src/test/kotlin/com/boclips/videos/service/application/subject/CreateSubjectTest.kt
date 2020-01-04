package com.boclips.videos.service.application.subject

import com.boclips.videos.api.request.VideoServiceApiFactory
import com.boclips.videos.service.application.exceptions.SubjectExistsException
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class CreateSubjectTest : AbstractSpringIntegrationTest() {
    @Test
    fun `throws when subject already exists`() {
        val createRequest = VideoServiceApiFactory.createSubjectRequest(name = "Music")

        createSubject(createRequest)

        assertThrows<SubjectExistsException> { createSubject(createRequest) }
    }
}

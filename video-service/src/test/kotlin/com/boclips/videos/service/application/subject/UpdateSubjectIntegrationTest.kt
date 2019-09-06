package com.boclips.videos.service.application.subject

import com.boclips.eventbus.events.subject.SubjectChanged
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class UpdateSubjectIntegrationTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var updateSubject: UpdateSubject

    @Test
    fun `publishes subject updated event`() {
        val savedSubjectId = saveSubject("Maths").id

        updateSubject.invoke(savedSubjectId, "Mathematicus")

        assertThat(fakeEventBus.countEventsOfType(SubjectChanged::class.java)).isEqualTo(1)
        assertThat((fakeEventBus.receivedEvents[0] as SubjectChanged).subject.name).isEqualTo("Mathematicus")
    }
}

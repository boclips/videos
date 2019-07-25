package com.boclips.videos.service.application.tag

import com.boclips.videos.service.domain.model.common.UserId
import com.boclips.videos.service.domain.model.tag.TagRepository
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class DeleteTagIntegrationTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var tagRepository: TagRepository

    @Autowired
    lateinit var deleteTag: DeleteTag

    @Test
    fun `when deleting tag, it deletes the tag from collections`() {
        val tagId = tagRepository.create("Biology").id

        deleteTag(tagId)

        assertThat(tagRepository.findById(tagId)).isNull()
    }
}

package com.boclips.videos.service.infrastructure.tag

import com.boclips.videos.service.domain.model.tag.TagRepository
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.TestFactories
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class MongoTagRepositoryIntegrationTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var mongoTagRepository: TagRepository

    @Test
    fun `find all tags`() {
        mongoTagRepository.create(label = "Mathematics")
        mongoTagRepository.create(label = "French")

        val tags = mongoTagRepository.findAll()

        assertThat(tags).hasSize(2)
        assertThat(tags.first().id).isNotNull
        assertThat(tags.first().label).isEqualTo("Mathematics")
    }

    @Test
    fun `find by ids`() {
        val maths = mongoTagRepository.create(label = "Mathematics")
        val nonExistingTagId = TestFactories.aValidId()

        val tags = mongoTagRepository.findByIds(listOf(maths.id.value, nonExistingTagId))

        assertThat(tags).containsExactly(maths)
    }

    @Test
    fun `create a tag`() {
        mongoTagRepository.create(label = "Mathematics")

        val tags = mongoTagRepository.findAll()

        assertThat(tags).hasSize(1)
        assertThat(tags.first().id).isNotNull
        assertThat(tags.first().label).isEqualTo("Mathematics")
    }

    @Test
    fun `delete a tag`() {
        val tag = mongoTagRepository.create(label = "Biology")

        mongoTagRepository.delete(tag.id)

        assertThat(mongoTagRepository.findAll()).isEmpty()
    }

    @Test
    fun `find by Id`() {
        val tag = mongoTagRepository.create(label = "Biology")

        val retrievedTag = mongoTagRepository.findById(tag.id)

        assertThat(retrievedTag).isNotNull
    }

    @Test
    fun `find by label`() {
        mongoTagRepository.create(label = "French")

        val retrievedTag = mongoTagRepository.findByLabel("French")

        assertThat(retrievedTag!!.label).isEqualTo("French")
    }
}

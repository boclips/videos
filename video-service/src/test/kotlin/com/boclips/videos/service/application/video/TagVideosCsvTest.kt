package com.boclips.videos.service.application.video

import com.boclips.videos.service.domain.model.taxonomy.CategoryCode
import com.boclips.videos.service.domain.model.taxonomy.CategoryWithAncestors
import com.boclips.videos.service.domain.service.video.VideoRepository
import com.boclips.videos.service.presentation.converters.CategoryMappingMetadata
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.CategoryFactory
import com.boclips.videos.service.testsupport.UserFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class TagVideosCsvTest : AbstractSpringIntegrationTest() {

    @Autowired
    private lateinit var tagVideosCsv: TagVideosCsv

    @Autowired
    private lateinit var videoRepository: VideoRepository

    @Test
    fun `tag single video with one category`() {
        saveCategory(CategoryFactory.sample(code = "A", description = "cat A"))
        saveCategory(CategoryFactory.sample(code = "C", description = "ANSI C"))

        val videoId = saveVideo(manualCategories = listOf("C"))

        tagVideosCsv(
            listOf(CategoryMappingMetadata(videoId = videoId.value, categoryCode = "A", tag = "", index = 1)),
            UserFactory.sample()
        )

        val retrieved = videoRepository.find(videoId)
        assertThat(retrieved!!.manualCategories).hasSize(2)
        assertThat(retrieved.manualCategories).containsExactlyInAnyOrder(
            CategoryWithAncestors(codeValue = CategoryCode("A"), description = "cat A"),
            CategoryWithAncestors(codeValue = CategoryCode("C"), description = "ANSI C")
        )
    }

    @Test
    fun `tag single video with one pedagogy tag`() {
        val otherTag = saveTag("Other")
        saveTag("Hook")

        val videoId = saveVideo()
        addPedagogyTagToVideo(videoId = videoId, tag = otherTag)

        listOf(CategoryMappingMetadata(videoId = videoId.value, categoryCode = "A", tag = "Hook", index = 1))

        val retrieved = videoRepository.find(videoId)
        assertThat(retrieved!!.tags).hasSize(1)
    }
}

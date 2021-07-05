package com.boclips.videos.service.application.video

import com.boclips.videos.service.domain.model.taxonomy.CategoryCode
import com.boclips.videos.service.domain.model.taxonomy.CategoryWithAncestors
import com.boclips.videos.service.domain.service.video.VideoRepository
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.CategoryFactory
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class TagVideosWithCategoriesTest : AbstractSpringIntegrationTest() {

    @Autowired
    private lateinit var tagVideosWithCategories: TagVideosWithCategories

    @Autowired
    private lateinit var videoRepository: VideoRepository

    @Test
    fun `tag single video with one category`() {
        addCategory(CategoryFactory.sample(code = "A", description = "cat A"))
        addCategory(CategoryFactory.sample(code = "C", description = "ANSI C"))

        val videoId = saveVideo(manualCategories = listOf("C"))

        tagVideosWithCategories(mapOf(videoId to listOf("A")))

        val retrieved = videoRepository.find(videoId)
        assertThat(retrieved!!.manualCategories).hasSize(2)
        assertThat(retrieved.manualCategories).containsExactlyInAnyOrder(
            CategoryWithAncestors(codeValue = CategoryCode("A"), description = "cat A"),
            CategoryWithAncestors(codeValue = CategoryCode("C"), description = "ANSI C")
        )
    }
}

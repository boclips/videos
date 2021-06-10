package com.boclips.videos.service.application.video

import com.boclips.users.api.response.accessrule.AccessRuleResource
import com.boclips.videos.service.application.exceptions.ContentPackageNotFoundException
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired

class GetVideosByContentPackageIntegrationTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var getVideosByContentPackage: GetVideosByContentPackage

    @Test
    fun `gets all videos included in content package`() {
        val video1 = saveVideo(title = "vid-1")
        val video2 = saveVideo(title = "vid-2")
        saveVideo(title = "vid-3")

        val accessRule = AccessRuleResource.IncludedVideos(
            name = "my-beautiful",
            videoIds = listOf(video1, video2).map { it.value }
        )

        saveContentPackage("package", "included videos", accessRule)

        val result = getVideosByContentPackage("package", pageSize = 50)

        assertThat(result.videoIds).containsExactlyInAnyOrder(video1, video2)
    }

    @Test
    fun `does not include private channels' videos in content package`() {
        val privateChannel = saveChannel("Channel to hide")
        saveVideo(title = "vid-1", existingChannelId = privateChannel.id.value)
        val video1 = saveVideo(title = "vid-3")
        val video2 = saveVideo(title = "vid-2")

        val accessRule = AccessRuleResource.IncludedVideos(
            name = "woi",
            videoIds = listOf(video1, video2).map { it.value }
        )

        saveContentPackage("package", "included videos", accessRule)

        val result = getVideosByContentPackage("package", pageSize = 50)

        assertThat(result.videoIds).containsExactlyInAnyOrder(video1, video2)
    }

    @Test
    fun `respects page size and index`() {
        saveVideo(title = "1")
        saveVideo(title = "2")
        saveVideo(title = "3")
        saveContentPackage("package", "package")

        val page1 = getVideosByContentPackage(
            "package",
            pageSize = 2
        )
        val page2 = getVideosByContentPackage(
            "package",
            pageSize = 2,
            cursorId = page1.cursor?.value
        )

        assertThat(page1.videoIds).hasSize(2)
        assertThat(page2.videoIds).hasSize(1)
    }

    @Test
    fun `throws exception when content package does not exist`() {
        assertThrows<ContentPackageNotFoundException> {
            getVideosByContentPackage(
                "some-package",
                pageSize = 10
            )
        }
    }
}

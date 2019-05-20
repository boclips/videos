package com.boclips.videos.service.client.internal.resources

import com.boclips.videos.service.client.SubjectId
import com.boclips.videos.service.client.VideoId
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.net.URI

class CollectionResourceTest {
    @Test
    fun `converts resource to collection`() {
        val resource = CollectionResource().apply {
            subjects = setOf(SubjectResource().apply { id = "maths" })
            videos = listOf(VideoResource().apply {
                _links = VideoLinks().apply { self = Link().apply { href = "https://video-service.com/v1/videos/123" } }
            })
        }

        val collection = resource.toCollection()
        assertThat(collection.subjects).containsExactly(SubjectId.builder().value("maths").build())
        assertThat(collection.videos).containsExactly(VideoId(URI("https://video-service.com/v1/videos/123")))
    }
}
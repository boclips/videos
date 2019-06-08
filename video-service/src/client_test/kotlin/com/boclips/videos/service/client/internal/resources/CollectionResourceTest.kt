package com.boclips.videos.service.client.internal.resources

import com.boclips.videos.service.client.SubjectId
import com.boclips.videos.service.client.VideoId
import com.boclips.videos.service.testsupport.TestFactories
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.net.URI

class CollectionResourceTest {
    @Test
    fun `converts resource to collection`() {
        val id = TestFactories.aValidId()
        val resource = CollectionResource().apply {
            title = "the title"
            subjects = setOf(SubjectResource().apply { this.id = "maths" })
            videos = listOf(VideoResource().apply {
                _links = VideoLinks().apply { self = Link().apply { href = "https://video-service.com/v1/videos/$id" } }
            })
        }

        val collection = resource.toCollection()
        assertThat(collection.title).isEqualTo("the title")
        assertThat(collection.subjects).containsExactly(SubjectId.builder().value("maths").build())
        assertThat(collection.videos).containsExactly(VideoId(URI("https://video-service.com/v1/videos/$id")))
    }
}

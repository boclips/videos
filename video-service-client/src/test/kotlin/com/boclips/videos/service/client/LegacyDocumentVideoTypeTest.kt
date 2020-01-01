package com.boclips.videos.service.client

//import org.assertj.core.api.Assertions.assertThat
//import org.assertj.core.api.Assertions.assertThatThrownBy
//import org.junit.jupiter.api.Test
//import com.boclips.videos.service.domain.model.video.ContentType
//
//class LegacyDocumentVideoTypeTest {
//
//    @Test
//    fun `client video types match video service video types`() {
//        val allVideoServiceTypes = ContentType.values().map { it.name }
//        val allVideoClientTypes = VideoType.values().map { it.name }
//
//        assertThat(allVideoServiceTypes).containsExactlyElementsOf(allVideoClientTypes)
//    }
//
//    @Test
//    fun `get type for video service type`() {
//        val id = ContentType.INSTRUCTIONAL_CLIPS.id
//
//        assertThat(VideoType.fromId(id)).isEqualTo(VideoType.INSTRUCTIONAL_CLIPS)
//    }
//
//    @Test
//    fun `throws for a bad id`() {
//        assertThatThrownBy { VideoType.fromId(-5) }.isInstanceOf(IllegalArgumentException::class.java)
//    }
//}

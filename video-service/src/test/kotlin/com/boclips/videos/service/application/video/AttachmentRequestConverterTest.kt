package com.boclips.videos.service.application.video

import com.boclips.videos.api.common.ExplicitlyNull
import com.boclips.videos.api.common.Specified
import com.boclips.videos.api.request.attachments.AttachmentRequest
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.domain.service.video.VideoUpdateCommand
import com.boclips.videos.service.testsupport.TestFactories.aValidId
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class AttachmentRequestConverterTest {
    @Test
    fun `when attachments are explicitely null`() {
        val result: VideoUpdateCommand? = AttachmentRequestConverter().convert(VideoId(aValidId()), ExplicitlyNull())

        assertThat(result!!).isInstanceOf(VideoUpdateCommand.RemoveAttachments::class.java)
    }

    @Test
    fun `when implicitly null`() {
        val result: VideoUpdateCommand? = AttachmentRequestConverter().convert(VideoId(aValidId()), null)

        assertThat(result).isNull()
    }

    @Test
    fun `when attachments are set`() {
        val result: VideoUpdateCommand? =
            AttachmentRequestConverter().convert(
                VideoId(aValidId()),
                Specified(
                    listOf(
                        AttachmentRequest(
                            linkToResource = "some-link",
                            description = "description",
                            type = "ACTIVITY"
                        )
                    )
                )
            )

        assertThat(result!!).isInstanceOf(VideoUpdateCommand.ReplaceAttachments::class.java)
    }
}

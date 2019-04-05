package com.boclips.videos.service.infrastructure.playback

import com.boclips.events.types.CaptionsFormat.VTT
import com.boclips.kalturaclient.captionasset.CaptionFormat
import com.boclips.videos.service.infrastructure.playback.CaptionAssetConverter.getCaptionAsset
import com.boclips.videos.service.testsupport.TestFactories.createCaptions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class CaptionAssetConverterTest {

    @Test
    fun `sets language`() {
        assertThat(getCaptionAsset(createCaptions(language = "en-US")).language).isEqualTo("English")
        assertThat(getCaptionAsset(createCaptions(language = "en-UK")).language).isEqualTo("English")
        assertThat(getCaptionAsset(createCaptions(language = "es-ES")).language).isEqualTo("Spanish")
        assertThat(getCaptionAsset(createCaptions(language = "fr-FR")).language).isEqualTo("French")
        assertThat(getCaptionAsset(createCaptions(language = "pl-PL")).language).isEqualTo("Polish")
    }

    @Test
    fun `appends suffix to labels of auto-generated captions`() {
        val captions = createCaptions(
                language = "es-ES",
                autoGenerated = true
        )
        assertThat(getCaptionAsset(captions).label).isEqualTo("Spanish (auto-generated)")
    }

    @Test
    fun `does not append a suffix to labels of human-generated captions`() {
        val captions = createCaptions(
                language = "es-ES",
                autoGenerated = false
        )
        assertThat(getCaptionAsset(captions).label).isEqualTo("Spanish")
    }

    @Test
    fun `sets format`() {
        assertThat(getCaptionAsset(createCaptions(format = VTT)).fileType).isEqualTo(CaptionFormat.WEBVTT)
    }
}

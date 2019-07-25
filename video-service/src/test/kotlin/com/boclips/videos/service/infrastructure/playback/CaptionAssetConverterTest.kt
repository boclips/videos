package com.boclips.videos.service.infrastructure.playback

import com.boclips.eventbus.domain.video.CaptionsFormat
import com.boclips.kalturaclient.captionasset.CaptionFormat
import com.boclips.kalturaclient.captionasset.KalturaLanguage
import com.boclips.videos.service.infrastructure.playback.CaptionAssetConverter.getCaptionAsset
import com.boclips.videos.service.testsupport.TestFactories.createCaptions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.*

class CaptionAssetConverterTest {

    @Test
    fun `sets language`() {
        assertThat(getCaptionAsset(createCaptions(language = Locale.US)).language).isEqualTo(KalturaLanguage.ENGLISH)
        assertThat(getCaptionAsset(createCaptions(language = Locale.UK)).language).isEqualTo(KalturaLanguage.ENGLISH)
        assertThat(getCaptionAsset(createCaptions(language = Locale.forLanguageTag("es-ES"))).language).isEqualTo(
            KalturaLanguage.SPANISH
        )
        assertThat(getCaptionAsset(createCaptions(language = Locale.FRENCH)).language).isEqualTo(KalturaLanguage.FRENCH)
        assertThat(getCaptionAsset(createCaptions(language = Locale.forLanguageTag("pl-PL"))).language).isEqualTo(
            KalturaLanguage.POLISH
        )
    }

    @Test
    fun `appends suffix to labels of auto-generated captions`() {
        val captions = createCaptions(
            language = Locale.forLanguageTag("es-ES"),
            autoGenerated = true
        )
        assertThat(getCaptionAsset(captions).label).isEqualTo("Spanish (auto-generated)")
    }

    @Test
    fun `does not append a suffix to labels of human-generated captions`() {
        val captions = createCaptions(
            language = Locale.forLanguageTag("es-ES"),
            autoGenerated = false
        )
        assertThat(getCaptionAsset(captions).label).isEqualTo("Spanish")
    }

    @Test
    fun `sets format`() {
        assertThat(getCaptionAsset(createCaptions(format = CaptionsFormat.VTT)).fileType).isEqualTo(CaptionFormat.WEBVTT)
    }
}

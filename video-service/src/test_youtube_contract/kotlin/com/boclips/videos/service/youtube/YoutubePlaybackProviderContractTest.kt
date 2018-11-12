package com.boclips.videos.service.youtube

import com.boclips.videos.service.domain.model.playback.PlaybackId
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType
import com.boclips.videos.service.domain.model.playback.YoutubePlayback
import com.boclips.videos.service.domain.service.PlaybackProvider
import com.boclips.videos.service.infrastructure.playback.TestYoutubePlaybackProvider
import com.boclips.videos.service.infrastructure.playback.YoutubePlaybackProvider
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import org.junit.jupiter.params.provider.ArgumentsSource
import org.yaml.snakeyaml.Yaml
import java.io.InputStream
import java.time.Duration
import java.util.stream.Stream


class YoutubePlaybackProviderContractTest {
    @ParameterizedTest
    @ArgumentsSource(PlaybackProviderArgumentProvider::class)
    fun `retrievePlayback adds youtube playback information`(playbackProvider: PlaybackProvider) {
        val playbackId = PlaybackId(type = PlaybackProviderType.YOUTUBE, value = "4IYDb6K5UF8")
        val youtubePlayback = playbackProvider.retrievePlayback(listOf(playbackId))[playbackId]!!

        assertThat(youtubePlayback).isInstanceOf(YoutubePlayback::class.java)

        val videoPlayback = youtubePlayback as YoutubePlayback

        assertThat(videoPlayback.youtubeId).isEqualTo("4IYDb6K5UF8")
        assertThat(videoPlayback.thumbnailUrl).isEqualTo("https://i.ytimg.com/vi/4IYDb6K5UF8/hqdefault.jpg")
        assertThat(videoPlayback.duration).isEqualTo(Duration.ofMinutes(1).plusSeconds(59))
    }

    @ParameterizedTest
    @ArgumentsSource(PlaybackProviderArgumentProvider::class)
    fun `retrievePlayback can deal with empty requests`(playbackProvider: PlaybackProvider) {
        val youtubePlayback = playbackProvider.retrievePlayback(emptyList())

        assertThat(youtubePlayback).isEmpty()
    }

    @ParameterizedTest
    @ArgumentsSource(PlaybackProviderArgumentProvider::class)
    fun `retrievePlayback can omits videos which cannot be located`(playbackProvider: PlaybackProvider) {
        val playbackId = PlaybackId(type = PlaybackProviderType.YOUTUBE, value = "1239123jkdsfajkadsfasdf")
        val youtubePlayback = playbackProvider.retrievePlayback(listOf(playbackId))

        assertThat(youtubePlayback).isEmpty()
    }
}

class PlaybackProviderArgumentProvider : ArgumentsProvider {
    override fun provideArguments(context: ExtensionContext?): Stream<out Arguments> {
        val testYoutubePlaybackProvider = TestYoutubePlaybackProvider()
                .addVideo("4IYDb6K5UF8", "https://i.ytimg.com/vi/4IYDb6K5UF8/hqdefault.jpg", Duration.ofMinutes(1).plusSeconds(59))
        val realYoutubePlaybackProvider = YoutubePlaybackProvider(readYoutubeApiKeyFromConf())

        return Stream.of(
                testYoutubePlaybackProvider,
                realYoutubePlaybackProvider
        ).map { playbackProvider -> Arguments.of(playbackProvider) }
    }
}

private fun readYoutubeApiKeyFromConf(): String {
    val key = "YOUTUBE_API_KEY"

    if (System.getenv(key) != null) {
        return System.getenv(key)
    }

    val yaml = Yaml()
    val inputStream: InputStream =
            YoutubePlaybackProviderContractTest::javaClass.javaClass.classLoader
                    .getResourceAsStream("contract-test-setup.yml")

    val apiKey = yaml.load<Map<String, String>>(inputStream)[key]!!
    inputStream.close()

    return apiKey
}

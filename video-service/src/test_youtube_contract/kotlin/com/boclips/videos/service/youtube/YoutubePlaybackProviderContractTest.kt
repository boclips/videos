package com.boclips.videos.service.youtube

import com.boclips.videos.service.domain.model.playback.PlaybackId
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType
import com.boclips.videos.service.domain.model.playback.YoutubePlayback
import com.boclips.videos.service.domain.service.PlaybackProvider
import com.boclips.videos.service.infrastructure.playback.TestYoutubePlaybackProvider
import com.boclips.videos.service.infrastructure.playback.YoutubePlaybackProvider
import com.boclips.videos.service.testsupport.TestFactories
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

class YoutubePlaybackProviderContractTest {
    @ParameterizedTest
    @ArgumentsSource(PlaybackProviderArgumentProvider::class)
    internal fun `getVideosWithPlayback adds youtube playback information`(playbackProvider: PlaybackProvider) {

        val video = TestFactories.createVideo(playbackId = PlaybackId(playbackProviderType = PlaybackProviderType.YOUTUBE, playbackId = "4IYDb6K5UF8"))

        val videoWithPlayback = playbackProvider.getVideosWithPlayback(listOf(video)).first()

        assertThat(videoWithPlayback.videoPlayback).isInstanceOf(YoutubePlayback::class.java)

        val videoPlayback = videoWithPlayback.videoPlayback as YoutubePlayback

        assertThat(videoPlayback.youtubeId).isEqualTo("4IYDb6K5UF8")
        assertThat(videoPlayback.thumbnailUrl).isEqualTo("https://i.ytimg.com/vi/4IYDb6K5UF8/hqdefault.jpg")
        assertThat(videoPlayback.duration).isEqualTo(Duration.ofMinutes(1).plusSeconds(59))
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
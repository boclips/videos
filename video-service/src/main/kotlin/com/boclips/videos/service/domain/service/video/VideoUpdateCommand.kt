package com.boclips.videos.service.domain.service.video

import com.boclips.videos.service.domain.model.asset.AssetId
import com.boclips.videos.service.domain.model.asset.LegacySubject
import com.boclips.videos.service.domain.model.asset.Topic
import com.boclips.videos.service.domain.model.playback.VideoPlayback
import java.time.Duration
import java.util.Locale

sealed class VideoUpdateCommand(val assetId: AssetId) {
    class ReplaceSubjects(assetId: AssetId, val subjects: List<LegacySubject>) : VideoUpdateCommand(assetId)
    class ReplaceDuration(assetId: AssetId, val duration: Duration) : VideoUpdateCommand(assetId)
    class ReplacePlayback(assetId: AssetId, val playback: VideoPlayback) : VideoUpdateCommand(assetId)
    class MakeSearchable(assetId: AssetId) : VideoUpdateCommand(assetId)
    class HideFromSearch(assetId: AssetId) : VideoUpdateCommand(assetId)
    class ReplaceLanguage(assetId: AssetId, val language: Locale) : VideoUpdateCommand(assetId)
    class ReplaceTranscript(assetId: AssetId, val transcript: String) : VideoUpdateCommand(assetId)
    class ReplaceTopics(assetId: AssetId, val topics: Set<Topic>) : VideoUpdateCommand(assetId)
    class ReplaceKeywords(assetId: AssetId, val keywords: Set<String>) : VideoUpdateCommand(assetId)
}


package com.boclips.videos.service.domain.service.video

import com.boclips.videos.service.domain.model.asset.AssetId
import com.boclips.videos.service.domain.model.asset.Subject
import java.time.Duration

abstract class VideoUpdateCommand(val assetId: AssetId)

class ReplaceSubjects(assetId: AssetId, val subjects: List<Subject>) : VideoUpdateCommand(assetId = assetId)
class ReplaceDuration(assetId: AssetId, val duration: Duration) : VideoUpdateCommand(assetId = assetId)

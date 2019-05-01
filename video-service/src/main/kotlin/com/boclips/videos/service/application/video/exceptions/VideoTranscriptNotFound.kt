package com.boclips.videos.service.application.video.exceptions

import com.boclips.videos.service.domain.model.asset.AssetId
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.BAD_REQUEST)
class VideoTranscriptNotFound(val assetId: AssetId) :
    VideoServiceException("Video transcript for asset '${assetId.value}' not found.")
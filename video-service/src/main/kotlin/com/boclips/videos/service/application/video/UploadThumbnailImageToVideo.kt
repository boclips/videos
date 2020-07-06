package com.boclips.videos.service.application.video

import com.boclips.kalturaclient.KalturaClient
import com.boclips.videos.api.request.video.SetThumbnailRequest
import java.io.InputStream

class UploadThumbnailImageToVideo(
    private val kalturaClient: KalturaClient,
    private val setVideoThumbnail: SetVideoThumbnail
) {
    operator fun invoke(videoId: String, playbackId: String?, imageStream: InputStream?, filename: String?) {
        kalturaClient.addThumbnailFromImage(playbackId, imageStream, filename).let { thumbAssetId ->
            kalturaClient.setThumbnailAsDefault(thumbAssetId)
            setVideoThumbnail(SetThumbnailRequest.SetCustomThumbnail(videoId, true))
        }
    }
}

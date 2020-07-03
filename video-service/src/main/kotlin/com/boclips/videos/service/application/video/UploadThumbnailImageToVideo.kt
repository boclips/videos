package com.boclips.videos.service.application.video

import com.boclips.kalturaclient.KalturaClient
import java.io.InputStream

class UploadThumbnailImageToVideo(
    private val kalturaClient: KalturaClient,
    private val setVideoThumbnail: SetVideoThumbnail
) {
    operator fun invoke(videoId: String, playbackId:String?, imageStream: InputStream?, filename: String?) {
        //upload
        //setAsDefault
        //set customThumbnail
    }

}

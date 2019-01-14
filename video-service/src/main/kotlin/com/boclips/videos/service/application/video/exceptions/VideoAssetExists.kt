package com.boclips.videos.service.application.video.exceptions

class VideoAssetExists(val contentPartnerId: String, val contentPartnerVideoId: String) : RuntimeException()

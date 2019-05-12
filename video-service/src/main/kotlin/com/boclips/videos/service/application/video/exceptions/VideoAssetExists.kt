package com.boclips.videos.service.application.video.exceptions

class VideoExists(val contentPartnerId: String, val contentPartnerVideoId: String) : RuntimeException()

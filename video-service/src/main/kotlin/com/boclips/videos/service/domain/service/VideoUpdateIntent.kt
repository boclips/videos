package com.boclips.videos.service.domain.service

import com.boclips.videos.service.domain.model.asset.Subject

sealed class VideoUpdateIntent

data class VideoSubjectsUpdate(val subjects: List<Subject>) : VideoUpdateIntent()

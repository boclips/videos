package com.boclips.contentpartner.service.domain.model

sealed class IngestDetails

object ManualIngest : IngestDetails()

object CustomIngest : IngestDetails()

data class MrssFeedIngest(val url: String) : IngestDetails()

data class YoutubeScrapeIngest(val url: String) : IngestDetails()




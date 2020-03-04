package com.boclips.contentpartner.service.domain.model

sealed class IngestDetails

object ManualIngest : IngestDetails()

object CustomIngest : IngestDetails()

class MrssFeedIngest(val url: String) : IngestDetails()

class YoutubeScrapeIngest(val url: String) : IngestDetails()




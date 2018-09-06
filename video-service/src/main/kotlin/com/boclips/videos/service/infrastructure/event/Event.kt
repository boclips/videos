package com.boclips.videos.service.infrastructure.event

import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "event-log")
open class Event<TData>(val type: String, val data: TData)

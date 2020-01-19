package com.boclips.search.service.domain.common

interface ProgressNotifier {
    fun send(message: String)
    fun complete() = send("OPERATION COMPLETE")
}

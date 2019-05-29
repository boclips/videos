package com.boclips.search.service.domain

interface ProgressNotifier {
    fun send(message: String)
    fun complete() = send("OPERATION COMPLETE")
}
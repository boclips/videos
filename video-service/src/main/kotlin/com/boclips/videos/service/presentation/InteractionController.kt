package com.boclips.videos.service.presentation

import com.boclips.videos.service.application.event.GetLatestInteractions
import com.boclips.videos.service.presentation.event.InteractionsFormatter
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/interactions")
class InteractionController(private val getLatestInteractions: GetLatestInteractions) {

    @GetMapping
    fun status(): String {
        return InteractionsFormatter.format(getLatestInteractions())
    }
}

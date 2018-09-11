package com.boclips.videos.service.presentation

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/v1/user")
class UserController {

    @GetMapping
    fun info() {
    }
}

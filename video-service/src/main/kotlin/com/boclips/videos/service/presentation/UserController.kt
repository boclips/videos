package com.boclips.videos.service.presentation

import org.springframework.hateoas.Resource
import org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo
import org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/v1/user")
class UserController {
    companion object {
        fun profileLink() = profileLinkBuilder().withRel("user")!!
        fun profileLinkBuilder() = linkTo(methodOn(UserController::class.java).profile())!!
    }

    @GetMapping
    fun profile() : Resource<String> = Resource("",
            profileLinkBuilder().withSelfRel()
    )
}

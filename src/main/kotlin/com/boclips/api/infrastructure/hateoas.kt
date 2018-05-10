package com.boclips.api.infrastructure

import org.springframework.hateoas.Resource
import org.springframework.hateoas.Resources
import reactor.core.publisher.Flux

fun <T> Flux<T>.toResourceOfResources() =
        this.map { Resource(it) }
        .collectList()
        .map { Resources(it) }
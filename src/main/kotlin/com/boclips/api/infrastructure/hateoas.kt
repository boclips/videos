package com.boclips.api.infrastructure

import org.springframework.hateoas.Link
import org.springframework.hateoas.Resource
import org.springframework.hateoas.Resources
import reactor.core.publisher.Flux

fun <T> Flux<T>.toResourceOfResources(linkSupplier: (T) -> Iterable<Link> = { listOf() }) =
        this.map { Resource(it, linkSupplier(it)) }
                .collectList()
                .map { Resources(it) }
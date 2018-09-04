package com.boclips.videos.service.presentation.resources

import org.springframework.hateoas.Resources
import org.springframework.hateoas.core.EmbeddedWrapper
import org.springframework.hateoas.core.EmbeddedWrappers
import kotlin.reflect.KClass

fun <T : Any> resourcesOf(items: List<T>, cls: KClass<T>): Resources<*> {
    if (items.isEmpty()) {
        val wrappers = EmbeddedWrappers(false)
        val wrapper = wrappers.emptyCollectionOf(cls.java)
        return Resources<EmbeddedWrapper>(listOf(wrapper))
    }

    return Resources<T>(items)
}
package com.boclips.videos.service.presentation.hateoas

import org.springframework.hateoas.Resource
import org.springframework.hateoas.core.EmbeddedWrappers

object HateoasEmptyCollection {
    inline fun <reified T> fixIfEmptyCollection(resources: List<Resource<T>>): List<*> {
        if (resources.isNotEmpty()) {
            return resources
        }
        return listOf(EmbeddedWrappers(false).emptyCollectionOf(T::class.java))
    }
}
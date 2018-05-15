package com.boclips.videos.presentation.resources

import org.springframework.hateoas.core.LinkBuilderSupport
import org.springframework.web.util.UriComponentsBuilder

class WebfluxLinkBuilder constructor(val builder: UriComponentsBuilder) : LinkBuilderSupport<WebfluxLinkBuilder>(builder) {

    override fun createNewInstance(builder: UriComponentsBuilder): WebfluxLinkBuilder {
        return WebfluxLinkBuilder(builder)
    }

    override fun getThis() = this

    companion object {

        fun fromContextPath(builder: UriComponentsBuilder) = WebfluxLinkBuilder(builder.replacePath(null))
    }
}
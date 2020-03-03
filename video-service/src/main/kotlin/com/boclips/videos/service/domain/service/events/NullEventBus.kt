package com.boclips.videos.service.domain.service.events

import com.boclips.eventbus.EventBus
import com.boclips.eventbus.EventHandler

class NullEventBus : EventBus {
    override fun <T : Any?> publish(event: MutableIterable<T>?) {

    }

    override fun <T : Any?> publish(event: T) {

    }

    override fun <T : Any?> subscribe(eventType: Class<T>?, eventHandler: EventHandler<T>?) {

    }

    override fun unsubscribe(eventType: Class<*>?) {

    }
}

package com.boclips.videos.service.config.tracing

import org.slf4j.MDC
import org.springframework.core.task.TaskDecorator

class TracingTaskDecorator : TaskDecorator {

    override fun decorate(runnable: Runnable): Runnable {
        val contextMap = MDC.getCopyOfContextMap()
        return Runnable {
            try {
                MDC.setContextMap(contextMap)
                runnable.run()
            } finally {
                MDC.clear()
            }
        }
    }
}

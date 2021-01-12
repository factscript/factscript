package org.factscript.language.execution.aws.lambda

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import org.factscript.language.execution.aws.event.EventService

class FlowlangTimerEventLambda : RequestHandler<Any, Any> {

    private val eventService = EventService()

    override fun handleRequest(input: Any?, context: Context?): Any {
        eventService.correlateTimerEvents()

        return "OK"
    }
}
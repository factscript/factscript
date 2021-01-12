package org.factscript.aws.lambda

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import org.factscript.language.definition.Flow
import org.factscript.language.execution.aws.lambda.*

abstract class FlowlangLambda : RequestHandler<Any, Any>{

    abstract fun definition() : Flow

    override fun handleRequest(input: Any?, context: Context?): Any {
        println("Input is $input")
//        if(true)
//throw RuntimeException("e")

        val inputMap = input as Map<String, *>
        val definition = definition()

        val lambdaContext = LambdaContext.of(definition, inputMap, context!!)

        println("Using context $lambdaContext")
        val handler = getHandler(lambdaContext)

        println("Using handler $handler")
        val handlerResult = handler.handle(lambdaContext)

        return handlerResult.toString()
    }

    private fun getHandler(lambdaContext: LambdaContext): LambdaHandler {
        val handlers = getHandlers()
        return handlers.first { handler -> handler.test(lambdaContext) }
    }

    open fun getHandlers(): List<LambdaHandler> {
        return listOf(InitializationHandler(),
                OnHandler(),
                ThrowingHandler(),
                MergeHandler(),
                LoopHandler(),
                InclusiveHandler(),
                ExclusiveHandler(),
                ExecutionHandler(),
                PromisingHandler(),
                CorrelatingHandler(),
                WaitingHandler(),
                GenericEventHandler(),
                EventGatewayHandler(),
                EventGatewayEvaluationHandler(),
                NoopHandler())
    }
}
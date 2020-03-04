package io.factdriven.aws.lambda

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import io.factdriven.aws.StateMachineService
import io.factdriven.aws.translation.FlowTranslator
import io.factdriven.definition.Definition
import io.factdriven.definition.typeName

abstract class FlowlangLambda : RequestHandler<Any, String>{

    private val stateMachineService = StateMachineService()

    override fun handleRequest(input: Any?, context: Context?): String? {
        val definition = definition()
        val name = name(definition)
        return stateMachineService.createOrUpdateStateMachine(name, FlowTranslator.translate(definition))
    }

    abstract fun definition() : Definition

    open fun name(definition: Definition) : String {
        return "${definition.typeName}StateMachine"
    }
}
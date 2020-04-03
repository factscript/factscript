package io.factdriven.aws.translation

import com.amazonaws.services.stepfunctions.builder.StateMachine
import io.factdriven.definition.api.Node

interface FlowTranslationStrategy<TRAVERSE> {
    fun translate(stateMachineBuilder: StateMachine.Builder, node: Node)
}
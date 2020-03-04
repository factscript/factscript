package io.factdriven.aws.translation

import com.amazonaws.services.stepfunctions.builder.StateMachine

interface FlowTranslationStrategy<NODE> {
    fun translate(stateMachineBuilder: StateMachine.Builder, node: NODE)
}
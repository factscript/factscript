package io.factdriven.aws.translation

import com.amazonaws.services.stepfunctions.builder.StateMachine
import io.factdriven.traverse.NodeTraverse

interface FlowTranslationStrategy<TRAVERSE> {
    fun translate(stateMachineBuilder: StateMachine.Builder, nodeTraverse: NodeTraverse)
}
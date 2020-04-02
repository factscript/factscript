package io.factdriven.aws.translation

import com.amazonaws.services.stepfunctions.builder.StateMachine
import com.amazonaws.services.stepfunctions.builder.StepFunctionBuilder
import com.amazonaws.services.stepfunctions.builder.StepFunctionBuilder.next
import com.amazonaws.services.stepfunctions.builder.conditions.NumericEqualsCondition
import com.amazonaws.services.stepfunctions.builder.states.Choice
import com.amazonaws.services.stepfunctions.builder.states.ChoiceState
import io.factdriven.definition.Definition
import io.factdriven.definition.api.Node
import io.factdriven.traverse.*

class FlowTranslator {

    companion object {
        fun translate(definition: Definition) : StateMachine {
            val stateMachineBuilder =  StepFunctionBuilder.stateMachine()
            FlowTranslator().translateToStateMachine(stateMachineBuilder, definition)
            return stateMachineBuilder.build()
        }
    }

    private fun translateToStateMachine(stateMachineBuilder: StateMachine.Builder, node: Node){
        val sequentialNodeTraverser = SequentialNodeTraverser(node)
        val fullTraverse = sequentialNodeTraverser.fullTraverse()
        var currentTraverse : Traverse? = fullTraverse.first()

        translateTraverse(currentTraverse, stateMachineBuilder)
    }

    private fun translateTraverse(traverse: Traverse?, stateMachineBuilder: StateMachine.Builder) {
        var currentTraverse = traverse
        while (currentTraverse != null) {
            if (currentTraverse is NodeTraverse) {
                if (currentTraverse.isStart()) {
                    stateMachineBuilder.startAt(currentTraverse.name())
                }
                translate(stateMachineBuilder, currentTraverse)
            } else if (currentTraverse is BlockTraverse) {
                translateBlock(stateMachineBuilder, currentTraverse)
            }
            currentTraverse = currentTraverse.next()
        }
    }

    private fun translateBlock(stateMachineBuilder: StateMachine.Builder, currentTraverse: BlockTraverse) {

        val choices = ArrayList<Choice.Builder>()

        for(path in currentTraverse.paths){
            val transition = Choice.builder().condition(NumericEqualsCondition.builder()
                    .expectedValue(0L).variable("$.output.my"))
                    .transition(next(path.next()?.name()))
            choices.add(transition)
        }

        stateMachineBuilder.state(currentTraverse.name(),
                ChoiceState.builder().choices(*choices.map { it }.toTypedArray()))

        translateTraverse(currentTraverse.next(), stateMachineBuilder)
    }

    private fun translate(stateMachineBuilder: StateMachine.Builder, traverse: NodeTraverse){
        val strategy = determineTranslationStrategy(traverse.current)
        strategy.translate(stateMachineBuilder, traverse)
    }

    private fun determineTranslationStrategy(node: Node): FlowTranslationStrategy<Node> {
        return ExecuteTranslationStrategy()
    }
}
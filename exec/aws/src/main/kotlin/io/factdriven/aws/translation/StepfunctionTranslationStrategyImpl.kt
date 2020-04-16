package io.factdriven.aws.translation

import com.amazonaws.services.stepfunctions.builder.StateMachine
import com.amazonaws.services.stepfunctions.builder.StepFunctionBuilder
import com.amazonaws.services.stepfunctions.builder.StepFunctionBuilder.next
import com.amazonaws.services.stepfunctions.builder.conditions.Condition
import com.amazonaws.services.stepfunctions.builder.conditions.StringEqualsCondition
import com.amazonaws.services.stepfunctions.builder.states.Choice
import com.amazonaws.services.stepfunctions.builder.states.ChoiceState
import com.amazonaws.services.stepfunctions.builder.states.NextStateTransition
import io.factdriven.definition.Branching
import io.factdriven.definition.Gateway.Exclusive
import io.factdriven.definition.Node
import io.factdriven.impl.utils.prettyJson
import io.factdriven.language.ConditionalExecution
import io.factdriven.language.Given
import java.util.stream.Collectors

class XOrTranslationStrategy(flowTranslator: FlowTranslator) : StepFunctionTranslationStrategy(flowTranslator) {
    override fun test(node: Node): Boolean {
        return node is Branching && node.gateway == Exclusive
    }

    override fun translate(stateMachineBuilder: StateMachine.Builder, node: Node) {
        val payload = Payload(id = node.id)
        val nodeParameter = NodeParameter(functionName = "PaymentRetrieval", payload = payload)

        stateMachineBuilder.state(name(node),
                StepFunctionBuilder.taskState()
                        .resource("arn:aws:states:::lambda:invoke.waitForTaskToken")
                        .parameters(nodeParameter.prettyJson)
                        .resultPath("$.output.condition")
                        .transition(next(nameGateway(node))))

        val choices = toChoices(stateMachineBuilder, node as Branching)
        stateMachineBuilder.state(nameGateway(node),
                ChoiceState.builder()
                        .choices(*choices)
        )
    }

    private fun nameGateway(node: Node) : String {
        return "gateway-"+name(node)
    }

    private fun toChoices(stateMachineBuilder: StateMachine.Builder, branching: Branching): Array<Choice.Builder> {
        return branching.children.stream()
                .map {node -> node as ConditionalExecution<*>}
                .peek { conditionalExecution -> flowTranslator.traverseAndTranslateNodes(conditionalExecution.children.first(), stateMachineBuilder) }
                .map {condition ->
                    Choice.builder().condition(toCondition(condition)).transition(transitionToNextOf(condition.children.first()) as NextStateTransition.Builder?)
                }.collect(Collectors.toList())
                .toTypedArray()
    }

    private fun toCondition(condition: ConditionalExecution<*>): Condition.Builder {
        return StringEqualsCondition.builder().variable("$.output.condition").expectedValue(name(condition))
    }
}

class FlowTranslationStrategy(flowTranslator: FlowTranslator) : StepFunctionTranslationStrategy(flowTranslator) {
    override fun test(node: Node): Boolean {
        return node.parent == null
    }

    override fun translate(stateMachineBuilder: StateMachine.Builder, node: Node) {
        stateMachineBuilder.startAt(name(node.children.first()))
        flowTranslator.traverseAndTranslateNodes(node.children.first(), stateMachineBuilder)
    }
}

class ExecuteTranslationStrategy(flowTranslator: FlowTranslator) : StepFunctionTranslationStrategy(flowTranslator) {

    override fun test(node: Node) : Boolean{
        return node.parent != null && node !is Branching && node !is Given<*>
    }

    override fun translate(stateMachineBuilder: StateMachine.Builder, node: Node) {
        val payload = Payload(id = node.id)
        val nodeParameter = NodeParameter(functionName = "PaymentRetrieval", payload = payload)

        if(node.isStart()) {
            nodeParameter.payload.messages = null
        }

        executionTranslation(stateMachineBuilder, node, nodeParameter)
    }

    private fun executionTranslation(stateMachineBuilder: StateMachine.Builder, node: Node, nodeParameter: NodeParameter) {
        stateMachineBuilder.state(name(node),
                StepFunctionBuilder.taskState()
                        .resource("arn:aws:states:::lambda:invoke.waitForTaskToken")
                        .parameters(nodeParameter.prettyJson)
                        .resultPath("$.Messages")
                        .transition(transitionToNextOf(node))
        )
    }
}

class GivenTranslator(flowTranslator: FlowTranslator) : StepFunctionTranslationStrategy(flowTranslator){
    override fun test(node: Node): Boolean {
        return node is Given<*>
    }

    override fun translate(stateMachineBuilder: StateMachine.Builder, node: Node) {
        // empty
    }
}

fun name(node: Node) : String{
    val name = node.id
    if(name.length > 70){ // temporary to prevent errors
        return name.substring(name.length - 60)
    }
    return name
}
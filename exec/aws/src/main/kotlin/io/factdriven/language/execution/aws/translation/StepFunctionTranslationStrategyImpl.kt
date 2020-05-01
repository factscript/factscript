package io.factdriven.language.execution.aws.translation

import com.amazonaws.services.stepfunctions.builder.StepFunctionBuilder
import com.amazonaws.services.stepfunctions.builder.StepFunctionBuilder.next
import com.amazonaws.services.stepfunctions.builder.conditions.BooleanEqualsCondition
import com.amazonaws.services.stepfunctions.builder.conditions.Condition
import com.amazonaws.services.stepfunctions.builder.conditions.StringEqualsCondition
import com.amazonaws.services.stepfunctions.builder.states.*
import io.factdriven.language.definition.Branching
import io.factdriven.language.definition.Gateway.*
import io.factdriven.language.definition.Node
import io.factdriven.language.impl.utils.prettyJson
import io.factdriven.language.ConditionalExecution
import io.factdriven.language.Given
import java.util.stream.Collectors

class ExclusiveTranslationStrategy(flowTranslator: FlowTranslator) : StepFunctionTranslationStrategy(flowTranslator) {
    override fun test(node: Node): Boolean {
        return node is Branching && node.gateway == Exclusive
    }

    override fun translate(translationContext: TranslationContext, node: Node) {
        val payload = Payload(id = node.id)
        val nodeParameter = NodeParameter(functionName = translationContext.lambdaFunction.name, payload = payload)

        translationContext.stepFunctionBuilder.state(toStateName(node),
                StepFunctionBuilder.taskState()
                        .resource(translationContext.lambdaFunction.resource)
                        .parameters(nodeParameter.prettyJson)
                        .resultPath("$.output.condition")
                        .transition(next(nameGateway(node))))

        val choices = toChoices(translationContext, node as Branching)
        translationContext.stepFunctionBuilder.state(nameGateway(node),
                ChoiceState.builder()
                        .choices(*choices)
        )
    }

    private fun nameGateway(node: Node) : String {
        return "gateway-"+toStateName(node)
    }

    private fun toChoices(translationContext: TranslationContext, branching: Branching): Array<Choice.Builder> {
        return branching.children.stream()
                .map {node -> node as ConditionalExecution<*>}
                .peek { conditionalExecution -> flowTranslator.translateGraph(translationContext, conditionalExecution.children.first()) }
                .map {condition ->
                    Choice.builder().condition(toCondition(condition)).transition(translationContext.transitionStrategy.nextTransition(condition.children.first()) as NextStateTransition.Builder?)
                }.collect(Collectors.toList())
                .toTypedArray()
    }

    private fun toCondition(condition: ConditionalExecution<*>): Condition.Builder {
        return StringEqualsCondition.builder().variable("$.output.condition").expectedValue(toStateName(condition))
    }
}

class InclusiveTranslationStrategy(flowTranslator: FlowTranslator) : StepFunctionTranslationStrategy(flowTranslator) {
    override fun test(node: Node): Boolean {
        return node is Branching && node.gateway == Inclusive
    }

    override fun translate(translationContext: TranslationContext, node: Node) {
        val payload = Payload(id = node.id)
        val nodeParameter = NodeParameter(functionName = translationContext.lambdaFunction.name, payload = payload)

        translationContext.stepFunctionBuilder.state(toStateName(node),
                StepFunctionBuilder.taskState()
                        .resource(translationContext.lambdaFunction.resource)
                        .parameters(nodeParameter.prettyJson)
                        .resultPath("$.output.condition")
                        .transition(next(nameGateway(node))))

        val choicesTranslationContext = translationContext.copyWith(transitionStrategy = InclusiveTransitionStrategy(node, node.forward))
        val choices = toChoices(choicesTranslationContext, node as Branching)
        translationContext.stepFunctionBuilder.state(nameGateway(node),
                ChoiceState.builder()
                        .choices(*choices)
        )

        translationContext.stepFunctionBuilder.state("while-${toStateName(node)}", ChoiceState.builder().choices(
                Choice.builder().condition(BooleanEqualsCondition.builder().variable("$.continue").expectedValue(true)).transition(next(toStateName(node))),
                Choice.builder().condition(BooleanEqualsCondition.builder().variable("$.continue").expectedValue(false)).transition(translationContext.transitionStrategy.nextTransition(node) as NextStateTransition.Builder)
        ))
    }

    private fun nameGateway(node: Node) : String {
        return "gateway-"+toStateName(node)
    }

    private fun toChoices(translationContext: TranslationContext, branching: Branching): Array<Choice.Builder> {
        return branching.children.stream()
                .map {node -> node as ConditionalExecution<*>}
                .peek { conditionalExecution -> flowTranslator.translateGraph(translationContext, conditionalExecution.children.first()) }
                .map {condition ->
                    Choice.builder().condition(toCondition(branching.children.indexOf(condition))).transition(translationContext.transitionStrategy.nextTransition(condition.children.first()) as NextStateTransition.Builder?)
                }.collect(Collectors.toList())
                .toTypedArray()
    }

    private fun toCondition(index: Int): Condition.Builder {
        return BooleanEqualsCondition.builder().variable("$.output.condition.$index").expectedValue(true)
    }
}

class ParallelTranslationStrategy(flowTranslator: FlowTranslator) : StepFunctionTranslationStrategy(flowTranslator){
    override fun test(node: Node): Boolean {
        return node is Branching && node.gateway == Parallel
    }

    override fun translate(translationContext: TranslationContext, node: Node) {
        val branches = translateBranches(translationContext, node as Branching)
        translationContext.stepFunctionBuilder.state(toStateName(node),
                ParallelState.builder()
                        .branches(*branches)
                        .transition(next("merge-" + toStateName(node)))
        )

        val nodeParameter = NodeParameter(functionName = translationContext.lambdaFunction.name, payload = ParallelMergePayload())

        translationContext.stepFunctionBuilder.state("merge-" + toStateName(node),
                StepFunctionBuilder.taskState()
                        .resource(translationContext.lambdaFunction.resource)
                        .parameters(nodeParameter.prettyJson)
                        .transition(translationContext.transitionStrategy.nextTransition(node))
        )
    }

    private fun translateBranches(translationContext: TranslationContext, branching: Branching): Array<Branch.Builder> {
        val branches = mutableListOf<Branch.Builder>()

        for (subFlow in branching.children) {
            val branch = Branch.builder().startAt(toStateName(subFlow.children.first()))
            val branchTranslationContext = translationContext.copyWith(transitionStrategy = ParallelTransitionStrategy(branching.forward), stepFunctionBuilder = BranchBuilder(branch))
            flowTranslator.translateGraph(branchTranslationContext, subFlow.children.first())
            branches.add(branch)
        }

        return branches.toTypedArray()
    }
}

class FlowTranslationStrategy(flowTranslator: FlowTranslator) : StepFunctionTranslationStrategy(flowTranslator) {
    override fun test(node: Node): Boolean {
        return node.parent == null
    }

    override fun translate(translationContext: TranslationContext, node: Node) {
        translationContext.stepFunctionBuilder.startAt(toStateName(node.children.first()))
        flowTranslator.translateGraph(translationContext, node.children.first())
    }
}

class ExecuteTranslationStrategy(flowTranslator: FlowTranslator) : StepFunctionTranslationStrategy(flowTranslator) {

    override fun test(node: Node) : Boolean{
        return node.parent != null && node !is Branching && node !is Given<*>
    }

    override fun translate(translationContext: TranslationContext, node: Node) {
        val payload = Payload(id = node.id)
        val nodeParameter = NodeParameter(functionName = translationContext.lambdaFunction.name, payload = payload)

        if(node.isStart()) {
            (nodeParameter.payload as Payload).messages = null
        }

        executionTranslation(translationContext, node, nodeParameter)
    }

    private fun executionTranslation(translationContext: TranslationContext, node: Node, nodeParameter: NodeParameter) {
        translationContext.stepFunctionBuilder.state(toStateName(node),
                StepFunctionBuilder.taskState()
                        .resource(translationContext.lambdaFunction.resource)
                        .parameters(nodeParameter.prettyJson)
                        .resultPath("$.Messages")
                        .transition(translationContext.transitionStrategy.nextTransition(node))
        )
    }
}

class GivenTranslator(flowTranslator: FlowTranslator) : StepFunctionTranslationStrategy(flowTranslator){
    override fun test(node: Node): Boolean {
        return node is Given<*>
    }

    override fun translate(translationContext: TranslationContext, node: Node) {
        // empty
    }
}

fun toStateName(prefix: String?, node: Node) : String{
    if(prefix != null){
        return "$prefix-${node.description}-${node.id}"
    }
    return "${node.description}-${node.id}"
}

fun toStateName(node: Node) : String{
    return toStateName(null, node)
}
package io.factdriven.language.execution.aws.translation

import com.amazonaws.services.stepfunctions.builder.StepFunctionBuilder
import com.amazonaws.services.stepfunctions.builder.conditions.*
import com.amazonaws.services.stepfunctions.builder.states.*
import io.factdriven.language.Loop
import io.factdriven.language.Option
import io.factdriven.language.definition.Branching
import io.factdriven.language.definition.Junction
import io.factdriven.language.definition.Node
import io.factdriven.language.execution.aws.translation.context.TranslationContext
import io.factdriven.language.execution.aws.translation.transition.InclusiveTransitionStrategy
import io.factdriven.language.execution.aws.translation.transition.LoopTransitionStrategy
import io.factdriven.language.execution.aws.translation.transition.ParallelTransitionStrategy
import io.factdriven.language.impl.utils.prettyJson
import java.util.stream.Collectors

class ExclusiveTranslationStrategy(flowTranslator: FlowTranslator) : StepFunctionTranslationStrategy(flowTranslator) {
    override fun test(node: Node): Boolean {
        return node is Branching && node.fork == Junction.One
    }

    override fun translate(translationContext: TranslationContext, node: Node) {
        val payload = Payload(id = node.id)
        val nodeParameter = NodeParameter(functionName = translationContext.lambdaFunction.name, payload = payload)

        translationContext.stepFunctionBuilder.state(translationContext.namingStrategy.getName(node),
                StepFunctionBuilder.taskState()
                        .resource(translationContext.lambdaFunction.resource)
                        .parameters(nodeParameter.prettyJson)
                        .resultPath("$.output.condition")
                        .transition(StepFunctionBuilder.next(nameGateway(translationContext, node))))

        val choices = toChoices(translationContext, node as Branching)
        translationContext.stepFunctionBuilder.state(nameGateway(translationContext, node),
                ChoiceState.builder()
                        .choices(*choices)
        )
    }

    private fun nameGateway(translationContext: TranslationContext, node: Node) : String {
        return translationContext.namingStrategy.getName("Gateway", node)
    }

    private fun toChoices(translationContext: TranslationContext, branching: Branching): Array<Choice.Builder> {
        val conditions = branching.children.stream()
                .map { node -> node as Option<*> }
                .peek { conditionalExecution -> flowTranslator.translateGraph(translationContext, conditionalExecution.children.first()) }
                .collect(Collectors.toList())

        return conditions
                .stream()
                .map { condition ->
                    Choice.builder().condition(toCondition(conditions.indexOf(condition)))
                            .transition(translationContext.transitionStrategy
                                    .nextTransition(translationContext, condition.children.first()) as NextStateTransition.Builder?)
                }.collect(Collectors.toList())
                .toTypedArray()
    }

    private fun toCondition(index: Int): Condition.Builder {
        return StringEqualsCondition.builder().variable("$.output.condition").expectedValue("$index")
    }
}

class InclusiveTranslationStrategy(flowTranslator: FlowTranslator) : StepFunctionTranslationStrategy(flowTranslator) {
    override fun test(node: Node): Boolean {
        return node is Branching && node.fork == Junction.Some
    }

    override fun translate(translationContext: TranslationContext, node: Node) {
        val payload = InclusivePayload(inclusiveContext = null, id = node.id)
        val nodeParameter = NodeParameter(functionName = translationContext.lambdaFunction.name, payload = payload)

        translationContext.stepFunctionBuilder.state(translationContext.namingStrategy.getName(node),
                StepFunctionBuilder.taskState()
                        .resource(translationContext.lambdaFunction.resource)
                        .parameters(nodeParameter.prettyJson)
                        .resultPath("$.InclusiveContext")
                        .transition(StepFunctionBuilder.next("evaluate-${translationContext.namingStrategy.getName(node)}")))

        val evaluationPayload = InclusivePayload(id = node.id)
        val evaluationNodeParameter = NodeParameter(functionName = translationContext.lambdaFunction.name, payload = evaluationPayload)

        translationContext.stepFunctionBuilder.state("evaluate-${translationContext.namingStrategy.getName(node)}",
                StepFunctionBuilder.taskState()
                        .resource(translationContext.lambdaFunction.resource)
                        .parameters(evaluationNodeParameter.prettyJson)
                        .resultPath("$.InclusiveContext")
                        .transition(StepFunctionBuilder.next(nameGateway(translationContext, node))))

        val choicesTranslationContext = translationContext.copyWith(transitionStrategy = InclusiveTransitionStrategy(node, node.forward))
        val choices = toChoices(choicesTranslationContext, node as Branching)
        translationContext.stepFunctionBuilder.state(nameGateway(translationContext, node),
                ChoiceState.builder()
                        .choices(*choices)
        )

        translationContext.stepFunctionBuilder.state("while-${translationContext.namingStrategy.getName(node)}", ChoiceState.builder().choices(
                Choice.builder().condition(NumericGreaterThanCondition.builder().variable("$.InclusiveContext.next").expectedValue(-1)).transition(StepFunctionBuilder.next("evaluate-${translationContext.namingStrategy.getName(node)}")),
                Choice.builder().condition(NumericEqualsCondition.builder().variable("$.InclusiveContext.next").expectedValue(-1)).transition(translationContext.transitionStrategy.nextTransition(translationContext, node) as NextStateTransition.Builder)
        ))
    }

    private fun nameGateway(translationContext: TranslationContext, node: Node) : String {
        return "gateway-"+translationContext.namingStrategy.getName(node)
    }

    private fun toChoices(translationContext: TranslationContext, branching: Branching): Array<Choice.Builder> {
        return branching.children.stream()
                .map {node -> node as Option<*> }
                .peek { conditionalExecution -> flowTranslator.translateGraph(translationContext, conditionalExecution.children.first()) }
                .map {condition ->
                    Choice.builder().condition(toCondition(branching.children.indexOf(condition))).transition(translationContext.transitionStrategy.nextTransition(translationContext, condition.children.first()) as NextStateTransition.Builder?)
                }.collect(Collectors.toList())
                .toTypedArray()
    }

    private fun toCondition(index: Int): Condition.Builder {
        return BooleanEqualsCondition.builder().variable("$.InclusiveContext.conditions.$index").expectedValue(true)
    }
}

class ParallelTranslationStrategy(flowTranslator: FlowTranslator) : StepFunctionTranslationStrategy(flowTranslator){
    override fun test(node: Node): Boolean {
        return node is Branching && node.fork == Junction.All
    }

    override fun translate(translationContext: TranslationContext, node: Node) {
        val branches = translateBranches(translationContext, node as Branching)
        translationContext.stepFunctionBuilder.state(translationContext.namingStrategy.getName(node),
                ParallelState.builder()
                        .branches(*branches)
                        .transition(StepFunctionBuilder.next(translationContext.namingStrategy.getName("Merge", node)))
        )

        val nodeParameter = NodeParameter(functionName = translationContext.lambdaFunction.name, payload = ParallelMergePayload())

        translationContext.stepFunctionBuilder.state(translationContext.namingStrategy.getName("Merge", node),
                StepFunctionBuilder.taskState()
                        .resource(translationContext.lambdaFunction.resource)
                        .parameters(nodeParameter.prettyJson)
                        .transition(translationContext.transitionStrategy.nextTransition(translationContext, node))
        )
    }

    private fun translateBranches(translationContext: TranslationContext, branching: Branching): Array<Branch.Builder> {
        val branches = mutableListOf<Branch.Builder>()

        for (subFlow in branching.children) {
            val branch = Branch.builder().startAt(translationContext.namingStrategy.getName(subFlow.children.first()))
            val branchTranslationContext = translationContext.copyWith(transitionStrategy = ParallelTransitionStrategy(branching.forward), stepFunctionBuilder = BranchBuilder(branch))
            flowTranslator.translateGraph(branchTranslationContext, subFlow.children.first())
            branches.add(branch)
        }

        return branches.toTypedArray()
    }
}

class LoopTranslationStrategy(flowTranslator: FlowTranslator) : StepFunctionTranslationStrategy(flowTranslator){
    override fun test(node: Node): Boolean {
        return node is Loop<*>
    }

    override fun translate(translationContext: TranslationContext, node: Node) {
        val first = node.children.first()
        val last = node.children[node.children.size-2]//node.children.last { node !is Until<*> }
        val loopTransitionStrategy  = LoopTransitionStrategy(last, "evaluate-${translationContext.namingStrategy.getName(node)}")

        val payload = LoopPayload(id = node.id, loopContext = null)
        val nodeParameter = NodeParameter(functionName = translationContext.lambdaFunction.name, payload = payload)

        translationContext.stepFunctionBuilder.state(translationContext.namingStrategy.getName(node),
                StepFunctionBuilder.taskState()
                        .resource(translationContext.lambdaFunction.resource)
                        .parameters(nodeParameter.prettyJson)
                        .resultPath("$.LoopContext")
                        .transition(StepFunctionBuilder.next(translationContext.namingStrategy.getName(first))))

        flowTranslator.translateGraph(translationContext.copyWith(transitionStrategy = loopTransitionStrategy), first)

        val evaluationPayload = LoopPayload(id = node.id)
        val evaluationNodeParameter = NodeParameter(functionName = translationContext.lambdaFunction.name, payload = evaluationPayload)

        translationContext.stepFunctionBuilder.state("evaluate-${translationContext.namingStrategy.getName(node)}",
                StepFunctionBuilder.taskState()
                        .resource(translationContext.lambdaFunction.resource)
                        .parameters(evaluationNodeParameter.prettyJson)
                        .resultPath("$.LoopContext")
                        .transition(StepFunctionBuilder.next("while-${translationContext.namingStrategy.getName(node)}")))

        translationContext.stepFunctionBuilder.state("while-${translationContext.namingStrategy.getName(node)}",
                StepFunctionBuilder.choiceState()
                        .choices(
                                Choice.builder()
                                        .condition(BooleanEqualsCondition.builder()
                                                .variable("$.LoopContext.continue")
                                                .expectedValue(true))
                                        .transition(StepFunctionBuilder.next(translationContext.namingStrategy.getName(first))),
                                Choice.builder()
                                        .condition(BooleanEqualsCondition.builder()
                                                .variable("$.LoopContext.continue")
                                                .expectedValue(false))
                                        .transition(translationContext.transitionStrategy.nextTransition(translationContext, node) as NextStateTransition.Builder)
                        ))

    }
}

package org.factscript.language.execution.aws.translation

import com.amazonaws.services.stepfunctions.builder.StepFunctionBuilder
import com.amazonaws.services.stepfunctions.builder.StepFunctionBuilder.next
import com.amazonaws.services.stepfunctions.builder.states.Catcher
import com.amazonaws.services.stepfunctions.builder.states.PassState
import org.factscript.language.Execute
import org.factscript.language.Given
import org.factscript.language.Until
import org.factscript.language.definition.*
import org.factscript.language.execution.aws.translation.context.TranslationContext
import org.factscript.language.execution.aws.translation.transition.LastTransitionStrategy
import org.factscript.language.impl.utils.prettyJson

class FlowTranslationStrategy(flowTranslator: FlowTranslator) : StepFunctionTranslationStrategy(flowTranslator) {
    override fun test(node: Node): Boolean {
        return node is PromisingFlow
    }

    override fun translate(translationContext: TranslationContext, node: Node) {
        translationContext.stepFunctionBuilder.startAt(translationContext.namingStrategy.getName(node.children.first()))
        flowTranslator.translateGraph(translationContext, node.children.first())
    }
}

class ExecuteTranslationStrategy(flowTranslator: FlowTranslator) : StepFunctionTranslationStrategy(flowTranslator) {

    override fun test(node: Node) : Boolean{
        return node.parent != null && (node is Execute<*> || node is Throwing || node is Correlating || node is Waiting)
    }

    override fun translate(translationContext: TranslationContext, node: Node) {
        val payload = Payload(id = node.id)
        val nodeParameter = NodeParameter(functionName = translationContext.lambdaFunction.name, payload = payload)

        if((node is Throwing || node is Correlating) && node !is Execute<*>){
            translationContext.snsContext.addTopic(node)
        }

        translationContext.stepFunctionBuilder.state(translationContext.namingStrategy.getName(node),
                StepFunctionBuilder.taskState()
                        .catchers(*getCatchers(translationContext, flowTranslator, node.children))
                        .resource(translationContext.lambdaFunction.resource)
                        .parameters(nodeParameter.prettyJson)
                        .resultPath("$.Messages")
                        .transition(translationContext.transitionStrategy.nextTransition(translationContext, node))
        )
    }

    private fun getCatchers(translationContext: TranslationContext, flowTranslator: FlowTranslator, children: List<Node> = emptyList()): Array<Catcher.Builder> {
        return children.filter { node -> node is Correlating }
                .map { correlating ->
                    val event = correlating.children.first()

                    val error = when (event) {
                        is Waiting -> {
                            "waiting"
                        }
                        is Consuming -> {
                            translationContext.snsContext.addTopic(event)
                            event.consuming.simpleName
                        }
                        else -> {
                            throw RuntimeException(event::class.toString())
                        }
                    }

                    val catcher = Catcher.builder()
                            .resultPath("$.Messages")
                            .errorEquals(error)
                            .transition(next(translationContext.namingStrategy.getName(event)))

                    val localTranslationContext = translationContext.copyWith(transitionStrategy = LastTransitionStrategy(correlating.lastChild,
                            translationContext.namingStrategy.getName(correlating.parent?.nextSibling!!)))

                    localTranslationContext.stepFunctionBuilder.state(localTranslationContext.namingStrategy.getName(event),
                            PassState.builder()
                                    .transition(localTranslationContext.transitionStrategy.nextTransition(localTranslationContext, event)))

                    flowTranslator.translateGraph(localTranslationContext, event.nextSibling)

                    catcher
                }.toTypedArray()
    }
}

class PromisingTranslationStrategy(flowTranslator: FlowTranslator) : StepFunctionTranslationStrategy(flowTranslator){
    override fun test(node: Node): Boolean {
        return false //node is Promising && node !is PromisingFlow && node !is Execute<*>
    }

    override fun translate(translationContext: TranslationContext, node: Node) {
        translationContext.snsContext.addTopic(node)
        translationContext.stepFunctionBuilder.state(translationContext.namingStrategy.getName(node),
                PassState.builder()
                        .transition(translationContext.transitionStrategy.nextTransition(translationContext, node)))
    }
}

class SkipTranslationStrategy(flowTranslator: FlowTranslator) : StepFunctionTranslationStrategy(flowTranslator){
    override fun test(node: Node): Boolean {
        return node is Given<*> || node is Until <*>
    }

    override fun translate(translationContext: TranslationContext, node: Node) {
        //skip
    }
}


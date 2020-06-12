package io.factdriven.language.execution.aws.translation

import com.amazonaws.services.stepfunctions.builder.StepFunctionBuilder
import com.amazonaws.services.stepfunctions.builder.states.PassState
import io.factdriven.language.Execute
import io.factdriven.language.Given
import io.factdriven.language.Until
import io.factdriven.language.definition.*
import io.factdriven.language.execution.aws.translation.context.TranslationContext
import io.factdriven.language.impl.utils.prettyJson

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

        executionTranslation(translationContext, node, nodeParameter)
    }

    private fun executionTranslation(translationContext: TranslationContext, node: Node, nodeParameter: NodeParameter) {
        translationContext.stepFunctionBuilder.state(translationContext.namingStrategy.getName(node),
                StepFunctionBuilder.taskState()
                        .resource(translationContext.lambdaFunction.resource)
                        .parameters(nodeParameter.prettyJson)
                        .resultPath("$.Messages")
                        .transition(translationContext.transitionStrategy.nextTransition(translationContext, node))
        )
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

class ThrowingTranslationStrategy(flowTranslator: FlowTranslator) : StepFunctionTranslationStrategy(flowTranslator){
    override fun test(node: Node): Boolean {
       return node is Throwing && node !is Execute<*>
    }

    override fun translate(translationContext: TranslationContext, node: Node) {
//        val throwing = node as Throwing
//        val throwingClass = throwing.throwing
//        val snsContext = translationContext.snsContext
//        val topicName = throwingClass.simpleName!!
//        snsContext.topics.add(topicName)
//
//        val snsParameter = SnsParameter(snsContext.getTopicArn(topicName), throwingClass.qualifiedName!!)
//
//        translationContext.stepFunctionBuilder.state(translationContext.namingStrategy.getName(node), TaskState.builder()
//                .resource(snsContext.resource)
//                .parameters(snsParameter.prettyJson)
//                .transition(translationContext.transitionStrategy.nextTransition(node)))
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


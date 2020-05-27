package io.factdriven.language.execution.aws.translation

import io.factdriven.language.definition.Node
import io.factdriven.language.execution.aws.translation.context.TranslationContext


abstract class StepFunctionTranslationStrategy(val flowTranslator: FlowTranslator) {
    abstract fun test(node: Node): Boolean
    abstract fun translate(translationContext: TranslationContext, node: Node)
}


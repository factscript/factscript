package org.factscript.language.execution.aws.translation

import org.factscript.language.definition.Node
import org.factscript.language.execution.aws.translation.context.TranslationContext


abstract class StepFunctionTranslationStrategy(val flowTranslator: FlowTranslator) {
    abstract fun test(node: Node): Boolean
    abstract fun translate(translationContext: TranslationContext, node: Node)
}


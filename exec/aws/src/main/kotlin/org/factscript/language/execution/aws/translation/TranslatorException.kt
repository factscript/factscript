package org.factscript.language.execution.aws.translation

import org.factscript.language.definition.Node
import java.lang.RuntimeException

class NoMatchingTranslatorFoundException(node: Node) : RuntimeException("No matching strategy found for node type ${node::class}") {
}

class MultipleMatchingStrategiesFoundExeption : RuntimeException {

    constructor(list: List<StepFunctionTranslationStrategy>) : super("Matching strategies are ${list.map { i -> i.javaClass.name }}")
    constructor(message: String?) : super(message)
    constructor(message: String?, cause: Throwable?) : super(message, cause)
    constructor(cause: Throwable?) : super(cause)
}
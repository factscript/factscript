package io.factdriven.aws.translation

import java.lang.RuntimeException

class NoMatchingTranslatorFoundException : RuntimeException() {
}

class MultipleMatchingStrategiesFoundExeption : RuntimeException {

    constructor(list: List<StepFunctionTranslationStrategy>) : super("Matching strategies are ${list.map { i -> i.javaClass.name }}")
    constructor(message: String?) : super(message)
    constructor(message: String?, cause: Throwable?) : super(message, cause)
    constructor(cause: Throwable?) : super(cause)
}
package io.factdriven.language

import io.factdriven.language.definition.Flow

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
@FlowLanguage
interface Execution<T: Any>: Flow {

    val emit: Emit<T>

    val issue: Issue<T>

    val await: Await<T>

    val execute: Execute<T>

    val select: Select<T>

    val loop: LoopingExecution<T>

}

@FlowLanguage
interface TriggeredExecution<T:Any>: Execution<T> {

    val on: On<T>

}

@FlowLanguage
interface ConditionalExecution<T: Any>: Execution<T> {

    val given: Given<T>

}

@FlowLanguage
interface LoopingExecution<T: Any>: Execution<T> {

    val until: Until<T>

    operator fun invoke(path: LoopingExecution<T>.() -> Unit)

}
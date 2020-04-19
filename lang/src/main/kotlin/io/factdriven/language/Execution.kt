package io.factdriven.language

import io.factdriven.definition.Flow

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
@FlowLang
interface Execution<T: Any>: Flow {

    val emit: Emit<T>

    val issue: Issue<T>

    val await: Await<T>

    val execute: Execute<T>

    val select: Select<T>

    val loop: Loop<T>

}

@FlowLang
interface ConditionalExecution<T: Any>: Execution<T> {

    val given: Given<T>

}


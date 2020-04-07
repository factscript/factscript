package io.factdriven.language

import io.factdriven.definition.Flow

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
@DslMarker annotation class FlowLang

@FlowLang
interface Flow<T:Any>: Api<T>, Execution<T>

@FlowLang
interface Api<T: Any> {

    val on: On<T>

}

@FlowLang
interface Execution<T: Any>: Flow {

    val emit: Emit<T>

    val issue: Issue<T>

    val consume: Consume<T>

    val execute: Execute<T>

    val select: Select<T>

}

interface Labeled<L> {

    operator fun invoke(case: String): L

}


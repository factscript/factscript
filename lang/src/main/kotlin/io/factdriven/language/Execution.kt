package io.factdriven.language

import io.factdriven.language.definition.Flow

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
@FlowLanguage
interface Execution<T: Any>: Flow, Time<T> {

    val emit: Emit<T>

    val issue: Issue<T>

    val await: Await<T>

    val execute: Execute<T>

    val select: Select<T>

    val loop: Loop<T>

}

@FlowLanguage
interface Catch<T:Any>: Execution<T> {

    val on: Await<T>

}

@FlowLanguage
interface Promise<T:Any>: Catch<T> {

    override val on: On<T>

}

@FlowLanguage
interface Option<T: Any>: Execution<T> {

    val given: Given<T>

}

@FlowLanguage
interface Loop<T: Any>: Execution<T> {

    val until: Until<T>

    operator fun invoke(path: Loop<T>.() -> Unit)

}
package org.factscript.language

import org.factscript.language.definition.Flow

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

    val repeat: Repeat<T>

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

    val otherwise: Otherwise<T, Unit>

}

@FlowLanguage
interface Loop<T: Any>: Execution<T> {

    val until: Until<T>

}
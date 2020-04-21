package io.factdriven.language

import kotlin.reflect.KClass

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
@FlowLanguage
interface Emit<T: Any>: EmitEvent<T>

@FlowLanguage
interface EmitEvent<T: Any> {

    infix fun <M: Any> event(type: KClass<M>): Sentence<T>

}


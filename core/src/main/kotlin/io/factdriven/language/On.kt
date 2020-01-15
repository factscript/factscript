package io.factdriven.language

import io.factdriven.definition.Catching
import io.factdriven.definition.CatchingImpl
import io.factdriven.definition.Node
import kotlin.reflect.KClass

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
@FlowLang
interface On<T: Any>: OnCommand<T>

@FlowLang
interface OnCommand<T: Any> {

    infix fun <M: Any> command(type: KClass<M>)

}

class OnImpl<T: Any>(override val parent: Node): On<T>, CatchingImpl<T>(parent) {

    override fun <M : Any> command(type: KClass<M>) {
        this.catchingType = type
    }

}
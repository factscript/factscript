package io.factdriven.lang

import io.factdriven.def.Definition
import io.factdriven.def.DefinitionImpl
import io.factdriven.def.Node
import java.lang.IllegalArgumentException
import kotlin.reflect.KClass
import kotlin.reflect.full.companionObjectInstance

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
@DslMarker annotation class FlowLang

@FlowLang
interface Flow<T:Any>: Api<T>, Execution<T> {

    companion object {

        inline fun <reified T: Any> define(type: KClass<T> = T::class, flow: Flow<T>.() -> Unit): Flow<T> {
            val definition = FlowImpl(type).apply(flow)
            Definition.register(definition)
            return definition
        }

    }

}

inline fun <reified T: Any> define(type: KClass<T> = T::class, flow: Flow<T>.() -> Unit): Flow<T> {
    return Flow.define(type, flow)
}

@FlowLang
interface Api<T: Any> {

    val on: On<T>

}

@FlowLang
interface Execution<T: Any> {

    val emit: Emit<T>

    val notice: Notice<T>

}

class FlowImpl<T:Any>(type: KClass<T>): Flow<T>, DefinitionImpl(type) {

    override val on: On<T>
        get() {
            val child = OnImpl<T>(this)
            children.add(child)
            return child
        }

    override val emit: Emit<T>
        get() {
            val child = EmitImpl<T>(this)
            children.add(child)
            return child
        }

    override val notice: Notice<T>
        get() {
            val child = NoticeImpl<T>(this)
            children.add(child)
            return child
        }

}

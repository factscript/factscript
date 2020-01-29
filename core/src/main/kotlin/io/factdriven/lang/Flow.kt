package io.factdriven.lang

import io.factdriven.def.Definition
import io.factdriven.def.DefinitionImpl
import kotlin.reflect.KClass

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

    val issue: Issue<T>

    val consume: Consume<T>

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

    override val issue: Issue<T>
        get() {
            val child = IssueImpl<T>(this)
            children.add(child)
            return child
        }

    override val consume: Consume<T>
        get() {
            val child = ConsumeImpl<T>(this)
            children.add(child)
            return child
        }

}

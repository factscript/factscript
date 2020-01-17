package io.factdriven.language

import io.factdriven.definition.Definition
import io.factdriven.definition.DefinitionImpl
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

        val all: Map<KClass<*>, Definition> = mutableMapOf()

        fun get(entityType: KClass<*>): Definition {
            return all[entityType] ?: {
                init(entityType)
                all[entityType] ?: throw IllegalArgumentException("Flow for entity type ${entityType.simpleName} is not defined!")
            }.invoke()
        }

        inline fun <reified T: Any> define(type: KClass<T> = T::class, flow: Flow<T>.() -> Unit): Flow<T> {
            val definition = FlowImpl(type).apply(flow)
            (all as MutableMap)[definition.entityType] = definition
            return definition
        }

        fun init(entityType: KClass<*>) {
            entityType.companionObjectInstance
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

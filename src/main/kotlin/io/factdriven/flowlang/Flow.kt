package io.factdriven.flowlang

import kotlin.reflect.KClass

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */

fun <A: Any> flow(flow: Flow<A>.() -> Unit): Flow<A> = Flow<A>().apply(flow)

class Flow<A: Any> {

    lateinit var flow: A

    val on: Catch<A>  get() = Catch()

    val perform: Call get() = Call()

    val choose: Choose<A> get() = Choose<A>()

    val create: Throw<A, Any> get() = Throw<A, Any>()

    fun <M: Any> type(type: KClass<M>, vars: (() -> Map<String, Any>)? = null): () -> Listener<M> {
        return {
            Listener(type, vars)
        }
    }

    infix fun labeled(label: String) { }

}

data class Listener<M: Any>(val type: KClass<M>, val vars: (() -> Map<String, Any>)? = null)

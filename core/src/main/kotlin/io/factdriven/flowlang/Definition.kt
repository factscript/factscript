package io.factdriven.flowlang

import kotlin.reflect.KClass

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */

fun <I: Instance> execute(definition: Definition<I>.() -> Unit): Definition<I> = Definition<I>().apply(definition)

class Definition<I: Instance> {

    lateinit var status: I

    val on: Trigger<I>  get() = TODO()

    val execute: Execution get() = TODO()

    val select: Selection<I> get() = TODO()

    val create: Action<I, Any> get() = TODO()

    infix fun <M: Message> type(type: KClass<M>): () -> Listener<M> {
        TODO()
    }

    infix fun <M: Message> pattern(pattern: M): () -> Listener<M> {
        TODO()
    }

    infix fun labeled(label: String) {
        TODO()
    }

}

data class Listener<M: Message>(val type: KClass<out M>, val correlation: (() -> Map<String, Any>)? = null, val assertion: ((M) -> Boolean)? = null)

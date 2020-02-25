package io.factdriven.language

import io.factdriven.definition.ConsumingImpl
import io.factdriven.definition.Node
import kotlin.reflect.KClass

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
@FlowLang
interface Consume<T: Any>: ConsumeEvent<T>

@FlowLang
interface ConsumeEvent<T: Any> {

    infix fun <M : Any> event(type: KClass<M>): ConsumeEventHaving<T>

}

@FlowLang
interface ConsumeEventHaving<T: Any> {

    infix fun having(property: String): ConsumeEventHavingMatch<T>

}

@FlowLang
interface ConsumeEventHavingMatch<T: Any> {

    infix fun match(value: T.() -> Any?)

}

class ConsumeImpl<T: Any>(parent: Node): Consume<T>, ConsumeEventHaving<T>, ConsumeEventHavingMatch<T>, ConsumingImpl(parent) {

    override fun <M : Any> event(type: KClass<M>): ConsumeEventHaving<T> {
        this.catchingType = type
        return this
    }

    override fun having(property: String): ConsumeEventHavingMatch<T> {
        this.catchingProperties.add(property)
        return this
    }

    override fun match(value: T.() -> Any?) {
        @Suppress("UNCHECKED_CAST")
        this.matchingValues.add(value as (Any.() -> Any?))
    }

}
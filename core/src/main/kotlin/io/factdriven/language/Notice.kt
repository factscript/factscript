package io.factdriven.language

import io.factdriven.definition.CatchingImpl
import io.factdriven.definition.Node
import kotlin.reflect.KClass

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
@FlowLang
interface Notice<T: Any>: NoticeEvent<T>

@FlowLang
interface NoticeEvent<T: Any> {

    infix fun <M : Any> event(type: KClass<M>): NoticeEventHaving<T>

}

@FlowLang
interface NoticeEventHaving<T: Any> {

    infix fun having(property: String): NoticeEventHavingMatch<T>

}

@FlowLang
interface NoticeEventHavingMatch<T: Any> {

    infix fun match(value: T.() -> Any?)

}

class NoticeImpl<T: Any>(override val parent: Node): Notice<T>, NoticeEventHaving<T>, NoticeEventHavingMatch<T>, CatchingImpl(parent) {

    override fun <M : Any> event(type: KClass<M>): NoticeEventHaving<T> {
        this.catchingType = type
        return this
    }

    override fun having(property: String): NoticeEventHavingMatch<T> {
        this.catchingProperties.add(property)
        return this
    }

    override fun match(value: T.() -> Any?) {
        @Suppress("UNCHECKED_CAST")
        this.matchingValues.add(value as (Any.() -> Any?))
    }

}
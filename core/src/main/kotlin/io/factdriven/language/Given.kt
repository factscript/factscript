package io.factdriven.language

import io.factdriven.definition.api.Executing
import io.factdriven.definition.impl.CheckingImpl
import kotlin.reflect.KClass

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
@FlowLang
interface Given<T: Any>: GivenCondition<T>, Labeled<Given<T>>

@FlowLang
interface GivenCondition<T: Any> {

    infix fun condition(condition: T.() -> Boolean): GivenCondition<T>

}

@FlowLang
interface ConditionalExecution<T: Any>: Execution<T> {

    val given: Given<T>

}

class GivenImpl<T: Any>(parent: Executing): Given<T>, CheckingImpl(parent) {

    override fun invoke(case: String): Given<T> {
        this.label = case
        return this
    }

    override fun condition(condition: T.() -> Boolean): GivenCondition<T> {
        @Suppress("UNCHECKED_CAST")
        this.condition = condition as Any.() -> Boolean
        return this
    }

}

class ConditionalExecutionImpl<T:Any>(type: KClass<T>, override val parent: Executing? = null): ConditionalExecution<T>, FlowImpl<T>(type, parent) {

    override val given: Given<T>
        get() {
            val child = GivenImpl<T>(this)
            children.add(child)
            return child
        }

}
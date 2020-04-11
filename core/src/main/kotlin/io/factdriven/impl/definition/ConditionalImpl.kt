package io.factdriven.impl.definition

import io.factdriven.definition.Conditional
import io.factdriven.definition.Node
import io.factdriven.execution.Type
import io.factdriven.execution.type
import io.factdriven.language.Given
import io.factdriven.language.GivenCondition

open class ConditionalImpl<T: Any>(parent: Node):

    Given<T>,

    Conditional,
    NodeImpl(parent)

{

    override var condition: (Any.() -> Boolean)? = null
    override lateinit var label: String protected set

    override val type: Type
        get() = Type(
            entity.type.context,
            Given::class.java.simpleName
        )

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

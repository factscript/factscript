package io.factdriven.language.impl.definition

import io.factdriven.execution.Type
import io.factdriven.execution.type
import io.factdriven.language.Given
import io.factdriven.language.Otherwise
import io.factdriven.language.Until
import io.factdriven.language.definition.*

open class ConditionalImpl<T: Any>(parent: Node):

    Given<T>,
    Otherwise<T, Given<T>>,
    Until<T>,

    Node,
    ConditionalNode,
    OptionalNode,
    NodeImpl(parent)

{

    override var condition: (Any.() -> Boolean)? = null
    override fun isDefault(): Boolean = condition == null

    override lateinit var description: String internal set

    override val type: Type
        get() = Type(
            entity.type.context,
            Given::class.java.simpleName
        )

    override fun invoke(case: String): Given<T> {
        this.description = case
        return this
    }

    override fun condition(condition: T.() -> Boolean) {
        @Suppress("UNCHECKED_CAST")
        this.condition = condition as Any.() -> Boolean
    }

}

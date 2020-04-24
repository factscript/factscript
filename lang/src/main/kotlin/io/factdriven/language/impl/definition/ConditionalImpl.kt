package io.factdriven.language.impl.definition

import io.factdriven.language.definition.Conditional
import io.factdriven.language.definition.Node
import io.factdriven.execution.Type
import io.factdriven.execution.type
import io.factdriven.language.Given
import io.factdriven.language.Until

open class ConditionalImpl<T: Any>(parent: Node):

    Given<T>,
    Until<T>,

    Conditional,
    NodeImpl(parent)

{

    override var condition: (Any.() -> Boolean)? = null
    override lateinit var label: String internal set

    override val type: Type
        get() = Type(
            entity.type.context,
            Given::class.java.simpleName
        )

    override fun invoke(case: String): Given<T> {
        this.label = case
        return this
    }

    override fun condition(condition: T.() -> Boolean) {
        @Suppress("UNCHECKED_CAST")
        this.condition = condition as Any.() -> Boolean
    }

}

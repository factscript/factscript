package org.factscript.language.impl.definition

import org.factscript.execution.Type
import org.factscript.execution.type
import org.factscript.language.Given
import org.factscript.language.Otherwise
import org.factscript.language.Until
import org.factscript.language.definition.*

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

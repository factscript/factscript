package io.factdriven.language.impl.definition

import io.factdriven.language.*
import io.factdriven.language.definition.Branching
import io.factdriven.language.definition.Flow
import io.factdriven.language.definition.Node
import io.factdriven.language.definition.Throwing
import java.time.LocalDateTime
import kotlin.reflect.KClass

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
open class FlowImpl<T:Any>(entity: KClass<T>, override val parent: Node? = null):

    Execution<T>,

    Flow,
    NodeImpl(parent, entity)

{

    override val emit: Emit<T>
        get() {
            val child = ThrowingImpl<T, Any>(this)
            children.add(child)
            return child
        }

    override val issue: Issue<T>
        get() {
            val child = ThrowingImpl<T, Any>(this)
            children.add(child)
            return child
        }

    override val await: Await<T>
        get() {
            val child = CorrelatingImpl<T>(this)
            children.add(child)
            return child
        }

    override val execute: Execute<T>
        get() {
            val child = ExecutingImpl<T>(this)
            children.add(child)
            return child
        }

    override val select: Select<T>
        get() {
            val child = BranchingImpl<T>(this)
            children.add(child)
            return child
        }

    override val forward: Node? get() = if (parent is Branching) parent?.forward else nextSibling ?: parent?.forward
    override val backward: Node? get() = if (parent is Branching) parent?.backward else previousSibling ?: parent?.backward

    override fun isSucceeding(): Boolean {
        return (children.lastOrNull() as? Throwing)?.isSucceeding() == true
    }

    override fun isFailing(): Boolean {
        return (children.lastOrNull() as? Throwing)?.isFailing() == true
    }

    override fun duration(period: T.() -> String): AwaitTimeDuration<T> {
        return WaitingImpl(this, period = period)
    }

    override fun duration(description: String, period: T.() -> String): AwaitTimeDuration<T> {
        return WaitingImpl(this, description= description, period = period)
    }

    override fun limit(date: T.() -> LocalDateTime): AwaitTimeLimit<T> {
        return WaitingImpl(this, limit = date)
    }

    override fun limit(description: String, date: T.() -> LocalDateTime): AwaitTimeLimit<T> {
        return WaitingImpl(this, description = description, limit = date)
    }

}

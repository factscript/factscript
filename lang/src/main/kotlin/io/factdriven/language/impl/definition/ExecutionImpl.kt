package io.factdriven.language.impl.definition

import io.factdriven.language.*
import io.factdriven.language.definition.*
import java.time.LocalDateTime
import kotlin.reflect.KClass

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
open class ExecutionImpl<T:Any>(entity: KClass<T>, override val parent: Node? = null):

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
            val child = AwaitingImpl<T>(this)
            children.add(child)
            return child
        }

    override val execute: Execute<T>
        get() {
            val child = CallingImpl<T>(this)
            children.add(child)
            return child
        }

    override val select: Select<T>
        get() {
            val child = BranchingImpl<T>(this)
            children.add(child)
            return child
        }

    override val loop: LoopingExecution<T>
        get() {
            @Suppress("UNCHECKED_CAST")
            val child = LoopingExecutionImpl<T>(entity as KClass<T>, this)
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

    override fun cycle(period: T.() -> String): AwaitTimeCycle<T> {
        TODO("Not yet implemented")
    }

    override fun cycle(description: String, period: T.() -> String): AwaitTimeCycle<T> {
        TODO("Not yet implemented")
    }

    override fun duration(period: T.() -> String): AwaitTimeDuration<T> {
        TODO("Not yet implemented")
    }

    override fun duration(description: String, period: T.() -> String): AwaitTimeDuration<T> {
        TODO("Not yet implemented")
    }

    override fun limit(date: T.() -> LocalDateTime): AwaitTimeLimit<T> {
        TODO("Not yet implemented")
    }

    override fun limit(description: String, date: T.() -> LocalDateTime): AwaitTimeLimit<T> {
        TODO("Not yet implemented")
    }

}

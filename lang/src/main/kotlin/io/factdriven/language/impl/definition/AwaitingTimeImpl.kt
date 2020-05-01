package io.factdriven.language.impl.definition

import io.factdriven.execution.Type
import io.factdriven.execution.type
import io.factdriven.language.*
import io.factdriven.language.definition.AwaitingTime
import io.factdriven.language.definition.Node
import io.factdriven.language.definition.Timer
import io.factdriven.language.definition.Timer.*
import java.time.LocalDateTime
import kotlin.reflect.KClass

class AwaitingTimeImpl<T: Any>(override var parent: Node?, entity: KClass<*>):

    AwaitTimeCycleFromLimitTimes<T>,
    AwaitTimeCycle<T>,
    AwaitTimeDuration<T>,
    AwaitTimeLimit<T>,

    AwaitingTime,
    NodeImpl(parent, entity)

{

    override lateinit var description: String
    override var from: (Any.() -> LocalDateTime)? = null; private set // cycle?, duration?
    override var limit: (Any.() -> LocalDateTime)? = null; private set // cycle?, limit
    override var period: (Any.() -> String)? = null; private set // cycle, duration
    override var times: (Any.() -> Int)? = null; private set // cycle
    override lateinit var timer: Timer

    override val type: Type get() = Type(entity.type.context, timer.name)

    @Suppress("UNCHECKED_CAST")
    constructor(
        parent: Node?,
        description: String? = null,
        period: (T.() -> String)? = null,
        from: (T.() -> LocalDateTime)? = null,
        limit: (T.() -> LocalDateTime)? = null,
        times: (T.() -> Int)? = null
    ): this(parent = null, entity = parent!!.entity) {
        period?.let { this.period = it as (Any.() -> String) }
        from?.let { this.from = it as (Any.() -> LocalDateTime) }
        limit?.let { this.limit = it as (Any.() -> LocalDateTime) }
        times?.let { this.times = it as (Any.() -> Int) }
        timer = times?.let { Cycle } ?: period?.let { Duration } ?: Limit
        this.description = description ?: super.description
    }

    @Suppress("UNCHECKED_CAST")
    override fun from(date: T.() -> LocalDateTime): AwaitTimeCycleFromLimitTimes<T> {
        this.from = date as Any.() -> LocalDateTime
        return this
    }

    @Suppress("UNCHECKED_CAST")
    override fun limit(date: T.() -> LocalDateTime): AwaitTimeCycleFromLimitTimes<T> {
        this.limit = date as Any.() -> LocalDateTime
        return this
    }

    @Suppress("UNCHECKED_CAST")
    override fun times(times: T.() -> Int): AwaitTimeCycleFromLimitTimes<T> {
        this.times = times as  Any.() -> Int
        return this
    }

    @Suppress("UNCHECKED_CAST")
    override fun times(times: Int): AwaitTimeCycleFromLimitTimes<T> {
        this.times = { times }
        return this
    }

}

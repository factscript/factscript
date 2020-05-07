package io.factdriven.language.impl.definition

import io.factdriven.execution.Type
import io.factdriven.execution.type
import io.factdriven.language.*
import io.factdriven.language.definition.Waiting
import io.factdriven.language.definition.Node
import io.factdriven.language.definition.Timer
import io.factdriven.language.definition.Timer.*
import java.time.LocalDateTime
import kotlin.reflect.KClass

class WaitingImpl<T: Any>(override var parent: Node?, entity: KClass<*>):

    AwaitTimeDuration<T>,
    AwaitTimeLimit<T>,

    Waiting,
    NodeImpl(parent, entity)

{

    override lateinit var description: String

    override lateinit var timer: Timer
    override var limit: (Any.() -> LocalDateTime)? = null; private set // cycle?, limit
    override var period: (Any.() -> String)? = null; private set // cycle, duration

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

        this.description = description ?: super.description

        period?.let { this.period = it as (Any.() -> String) }
        limit?.let { this.limit = it as (Any.() -> LocalDateTime) }
        timer = period?.let { Duration } ?: Limit

    }

}

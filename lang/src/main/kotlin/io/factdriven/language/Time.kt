package io.factdriven.language

import java.time.LocalDateTime

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
@FlowLanguage
interface Time<T: Any>: TimeDuration<T>, TimeLimit<T>

@FlowLanguage
interface AwaitTime<T: Any> {

    infix fun time (duration: AwaitTimeDuration<T>)
    infix fun time (limit: AwaitTimeLimit<T>)

}

@FlowLanguage
interface TimeDuration<T: Any> {

    fun duration(period: T.() -> String): AwaitTimeDuration<T>
    fun duration(description: String, period: T.() -> String): AwaitTimeDuration<T>

}

@FlowLanguage
interface TimeLimit<T: Any> {

    fun limit(date: T.() -> LocalDateTime): AwaitTimeLimit<T>
    fun limit(description: String, date: T.() -> LocalDateTime): AwaitTimeLimit<T>

}

@FlowLanguage
interface AwaitTimeDuration<T>

@FlowLanguage
interface AwaitTimeLimit<T>

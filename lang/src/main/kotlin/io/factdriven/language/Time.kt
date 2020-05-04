package io.factdriven.language

import java.time.LocalDateTime

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
@FlowLanguage
interface Time<T: Any>: TimeCycle<T>, TimeDuration<T>, TimeLimit<T>

@FlowLanguage
interface AwaitTime<T: Any> {

    infix fun time (cycle: AwaitTimeCycle<T>): AwaitTimeCycleFromLimitTimes<T>
    infix fun time (duration: AwaitTimeDuration<T>): AwaitTimeFrom<T, Unit>
    infix fun time (limit: AwaitTimeLimit<T>)

}

@FlowLanguage
interface TimeCycle<T: Any> {

    fun cycle(period: T.() -> String): AwaitTimeCycle<T>
    fun cycle(description: String, period: T.() -> String): AwaitTimeCycle<T>

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
interface AwaitTimeCycle<T>

@FlowLanguage
interface AwaitTimeDuration<T>

@FlowLanguage
interface AwaitTimeLimit<T>

@FlowLanguage
interface AwaitTimeCycleFromLimitTimes<T>: AwaitTimeFrom<T, AwaitTimeCycleFromLimitTimes<T>>, AwaitTimeCycleLimit<T, AwaitTimeCycleFromLimitTimes<T>>,
    AwaitTimeCycleTimes<T, AwaitTimeCycleFromLimitTimes<T>>

interface AwaitTimeFrom<T,O> {

    infix fun from(date: T.() -> LocalDateTime): O

}

@FlowLanguage
interface AwaitTimeCycleLimit<T,O> {

    infix fun limit(date: T.() -> LocalDateTime): O

}

@FlowLanguage
interface AwaitTimeCycleTimes<T,O> {

    infix fun times(times: T.() -> Int): O
    infix fun times(times: Int): O

}

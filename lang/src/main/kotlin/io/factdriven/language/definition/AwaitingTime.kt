package io.factdriven.language.definition

import java.time.LocalDateTime

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
interface AwaitingTime: Catching {

    val timer: Timer
    val from: (Any.() -> LocalDateTime)?
    val limit: (Any.() -> LocalDateTime)?
    val period: (Any.() -> String)?
    val times: (Any.() -> Int)?

}

enum class Timer { Limit, Duration, Cycle }
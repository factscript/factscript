package io.factdriven.language.definition

import java.time.LocalDateTime

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
interface Waiting: Catching {

    val timer: Timer
    val limit: (Any.() -> LocalDateTime)?
    val period: (Any.() -> String)?

}

enum class Timer { Limit, Duration /*, Cycle */ }
package io.factdriven.execution.camunda.diagram

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
data class Arrow(val from: Box, val to: Box, val via: Position? = null) {

    fun wayPoints(): List<Position> {
        TODO()
    }

}

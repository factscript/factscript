package io.factdriven.execution.camunda.model

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
data class Dimension(val width: Int, val height: Int) {

    operator fun div(int: Int): Dimension {
        return Dimension(width / int, height / int)
    }

    operator fun times(int: Int): Dimension {
        return Dimension(width * int, height * int)
    }

}
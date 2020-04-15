package io.factdriven.execution.camunda.model

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
data class Position (val x: Int, val y: Int) {

    operator fun plus(position: Position): Position {
        return Position(x + position.x, y + position.y)
    }

    operator fun minus(position: Position): Position {
        return Position(x - position.x, y - position.y)
    }

    operator fun plus(dimension: Dimension): Position {
        return Position(x + dimension.width, y + dimension.height)
    }

    operator fun minus(dimension: Dimension): Position {
        return Position(x - dimension.width, y - dimension.height)
    }

}
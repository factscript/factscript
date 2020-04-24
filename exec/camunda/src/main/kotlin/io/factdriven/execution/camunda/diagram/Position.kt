package io.factdriven.execution.camunda.diagram

import io.factdriven.execution.camunda.model.BpmnModel

data class Position (val x: Int, val y: Int) {

    companion object {

        var Zero = Position(0,0)

    }

    operator fun plus(position: Position): Position {
        return Position(x + position.x, y + position.y)
    }

    operator fun minus(position: Position): Position {
        return Position(x - position.x, y - position.y)
    }

    operator fun plus(dimension: Dimension): Position {
        return this move dimension
    }

    operator fun minus(dimension: Dimension): Position {
        return this move dimension * - 1
    }

    infix fun move(dimension: Dimension): Position {
        return Position(
            x + dimension.width,
            y + dimension.height
        )
    }

    infix fun north(dimension: Dimension): Position {
        return north(dimension.height)
    }

    infix fun south(dimension: Dimension): Position {
        return south(dimension.height)
    }

    infix fun east(dimension: Dimension): Position {
        return east(dimension.width)
    }

    infix fun west(dimension: Dimension): Position {
        return west(dimension.width)
    }

    infix fun north(steps: Int): Position {
        return Position(x, y - steps)
    }

    infix fun south(steps: Int): Position {
        return Position(x, y + steps)
    }

    infix fun east(steps: Int): Position {
        return Position(x + steps, y)
    }

    infix fun west(steps: Int): Position {
        return Position(x - steps, y)
    }

}
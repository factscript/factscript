package io.factdriven.execution.camunda.diagram

import io.factdriven.execution.camunda.model.BpmnModel

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

    operator fun plus(dimension: Dimension): Dimension {
        return Dimension(
            width + dimension.width,
            height + dimension.height
        )
    }

    operator fun minus(dimension: Dimension): Dimension {
        return Dimension(
            width - dimension.width,
            height - dimension.height
        )
    }

}

val List<Dimension>.sumHeight: Int get() = sumBy { it.height }
val List<Dimension>.sumWidth: Int get() = sumBy { it.width }
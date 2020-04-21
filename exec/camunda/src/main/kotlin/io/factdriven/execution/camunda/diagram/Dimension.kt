package io.factdriven.execution.camunda.diagram

import io.factdriven.execution.camunda.model.BpmnModel

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
data class Dimension(val width: Int, val height: Int) {

    val inner get() = this - BpmnModel.margin * 2
    val outer get() = this + BpmnModel.margin * 2

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

val List<Dimension>.maxHeight: Int get() = maxByHeight?.height ?: 0
val List<Dimension>.maxWidth: Int get() = maxByWidth?.width ?: 0
val List<Dimension>.maxByHeight: Dimension? get() = maxBy { it.height }
val List<Dimension>.maxByWidth: Dimension? get() = maxBy { it.width }
val List<Dimension>.sumHeight: Int get() = sumBy { it.height }
val List<Dimension>.sumWidth: Int get() = sumBy { it.width }
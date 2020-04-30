package io.factdriven.language.visualization.bpmn.diagram

import io.factdriven.language.impl.utils.asType

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
data class Attached (val attach: Box, val to: Box, val directions: List<Direction> = emptyList()) {

    val position: Position get() {
        val rawTo = (to as? Artefact)?.raw ?: to
        val rawAttach = (attach as? Artefact)?.raw ?: attach
        return rawTo.position + (rawTo.north / 2) - (rawAttach.north) - rawAttach.west +
            when (directions.getOrNull(0)) {
                Direction.North -> if (directions.getOrNull(1) == Direction.East) Position(0,0) else rawTo.north
                else -> if (directions.getOrNull(1) == Direction.East) rawTo.south - rawTo.north else rawTo.south
            }
    }

    val original: Position get() {
        return attach.position.let { attach.internalPosition }
    }

}

enum class Direction {
    North, West, South, East
}
package io.factdriven.language.visualization.bpmn.diagram

import io.factdriven.language.impl.utils.asType

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
data class Attached (val attach: Box, val to: Box, val directions: List<Direction> = emptyList()) {

    val position: Position get() {
        val rawTo = (to as? Artefact)?.raw ?: to
        val rawAttach = (attach as? Artefact)?.raw ?: attach
        val south = directions.getOrNull(0) == Direction.South
        val east = directions.getOrNull(1) == Direction.East
        return rawTo.position - (rawAttach.west) +
            (if (east) rawTo.north / -2  else rawTo.north / 2) +
            (if (south) rawTo.west * 2 else Position(0,0))
    }

    val original: Position get() {
        return attach.position.let { attach.internalPosition }
    }

}

enum class Direction {
    North, West, South, East
}
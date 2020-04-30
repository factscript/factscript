package io.factdriven.language.visualization.bpmn.diagram

import io.factdriven.language.impl.utils.asType

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
data class Attached (val attach: Box, val to: Box, val directions: List<Direction> = emptyList()) {

    val position: Position get() {
        val rawTo = (to as? Artefact)?.raw ?: to
        val rawAttach = (attach as? Artefact)?.raw ?: attach
        return rawTo.position + rawTo.south - rawAttach.west
    }

    val original: Position get() {
        return attach.position.let { attach.internalPosition }
    }

}

enum class Direction {
    North, West, South, East
}
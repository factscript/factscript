package io.factdriven.language.visualization.bpmn.diagram

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
data class Arrow(val from: Box, val to: Box, val via: Box? = null) {

    val waypoints: List<Position> get() {

        val source = from.eastEntry?.raw ?: from
        val target = to.westEntry?.raw ?: to
        var west = source.position + source.east
        var east = target.position + target.west
        val via = via?.let { it.position + it.west }

        if (west.y == east.y) {

            if (via != null && via.y != west.y) {
                west = source.position + source.let { if (west.y < via.y ) it.south else it.north }
                east = target.position + target.let { if (east.y < via.y) it.south else it.north }
                return listOf(west, Position(west.x, via.y), Position(east.x, via.y), east)
            } else {
                return listOf(west, east)
            }

        } else {

            fun Box.start(): Box = western?.start() ?: northern?.start() ?: southern?.start() ?: this

            return if ((from.position + from.west).y == (from.start().position + from.start().west).y) {
                west = source.position + source.let { if (west.y < east.y) it.south else it.north }
                listOf(west, Position(west.x, east.y), east)
            } else {
                east = target.position + target.let { if (west.y < east.y) it.north else it.south }
                listOf(west, Position(east.x, west.y), east)
            }

        }

    }

}

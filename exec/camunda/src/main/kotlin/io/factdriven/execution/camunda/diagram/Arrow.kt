package io.factdriven.execution.camunda.diagram

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
data class Arrow private constructor (val from: Box, val to: Box, val passingBy: Position? = null, val xIsStickierThanY: Boolean = true) {

    constructor(from: Box, to: Box, passingBy: Position? = null): this(from, to, passingBy, true)
    constructor(from: Box, to: Box, xIsStickierThanY: Boolean): this(from, to, null, xIsStickierThanY)

    fun wayPoints(): List<Position> {
        val from = from.exitArtefact.inner
        val to = to.entryArtefact.inner
        var fromPos = from.position + from.rightExit
        var toPos = to.position + to.leftEntry
        if (fromPos.y == toPos.y) {
            if (passingBy != null && passingBy.y != fromPos.y) {
                return listOf(fromPos, Position(fromPos.x, passingBy.y), Position(toPos.x, passingBy.y), toPos)
            } else {
                return listOf(fromPos, toPos)
            }
        } else if (xIsStickierThanY) {
            fromPos = from.position + from.let { if (fromPos.y < toPos.y) it.bottomExit else it.topEntry }
            return listOf(fromPos, Position(fromPos.x, toPos.y), toPos)
        } else {
            toPos = to.position + to.let { if (fromPos.y < toPos.y) it.topEntry else it.bottomExit }
            return listOf(fromPos, Position(toPos.x, fromPos.y), toPos)
        }
    }

}

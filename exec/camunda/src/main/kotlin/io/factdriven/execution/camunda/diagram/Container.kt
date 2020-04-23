package io.factdriven.execution.camunda.diagram

class Container: Box() {

    lateinit var entry: Box
    lateinit var exit: Box

    val contains: Set<Box> get() = entry.allSiblings

    override val entryArtefact: Artefact get() = entry.entryArtefact
    override val exitArtefact: Artefact get() = exit.exitArtefact
    override val allArrows: Set<Arrow> get() = (arrows + contains.map { it.arrows }.flatten()).toSet()

    override val dimension: Dimension
        get() = Dimension(
            width = contains.map { it.allHorizontal.map { it.dimension }.sumWidth }.max()!!,
            height = contains.map { it.allVertical.map { it.dimension }.sumHeight }.max()!!
        )

    override val leftEntry: Position
        get() = Position(
            0,
            entry.y
        )
    override val rightExit: Position
        get() = Position(
            dimension.width,
            exit.y
        )
    override val topEntry: Position
        get() = Position(
            entry.topEntry.x,
            0
        )

    override val bottomExit: Position
        get() = Position(
            exit.topEntry.x,
            dimension.height
        )

}
package io.factdriven.execution.camunda.diagram

class Container: Box() {

    lateinit var westend: Box
    lateinit var eastend: Box

    val contained: Set<Box> get() = westend.connected

    override val westEntry: Artefact get() = westend.westEntry
    override val eastEntry: Artefact get() = eastend.eastEntry

    override val allArrows: Set<Arrow> get() = (arrows + contained.map { it.arrows }.flatten()).toSet()

    override val dimension: Dimension get() = Dimension(
        width = contained.map { box -> box.latitudes.map { it.dimension }.sumWidth }.max()!!,
        height = contained.map { box -> box.longitudes.map { it.dimension }.sumHeight }.max()!!
    )

    override val west: Position get() = Position(0, westend.equator)
    override val east: Position get() = Position(dimension.width, eastend.equator)
    override val north: Position get() = Position(westend.north.x, 0)
    override val south: Position get() = Position(eastend.north.x, dimension.height)

}
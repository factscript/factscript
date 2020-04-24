package io.factdriven.execution.camunda.diagram

class Container(val emptyHeight: Int = 0): Box() {

    constructor(vararg artefact: Artefact): this() {
        artefact.forEach {
            it.insideOf(this)
        }
    }

    var westend: Box? = null
    var eastend: Box? = null

    val contained: Set<Box> get() = westend?.connected ?: emptySet()

    override val westEntry: Artefact? get() = westend?.westEntry
    override val eastEntry: Artefact? get() = eastend?.eastEntry

    override val allArrows: Set<Arrow> get() = (arrows + contained.map { it.arrows }.flatten()).toSet()

    private lateinit var internalDimension: Dimension

    override val dimension: Dimension get()  {
        if (!::internalDimension.isInitialized) {
            internalDimension = Dimension(
                width = longitudes.map {
                    if (it is Container) (it.contained.map { box -> box.latitudes.map { it.dimension }.sumWidth }.max()
                        ?: 0) else it.dimension.width
                }.max()!!,
                height = contained.map { box -> box.longitudes.map { it.dimension }.sumHeight }.max() ?: emptyHeight
            )
        }
        return internalDimension
    }

    override val west: Position get() = Position(0, westend?.equator ?: dimension.height / 2)
    override val east: Position get() = Position(dimension.width, eastend?.equator ?: dimension.height / 2)
    override val north: Position get() = Position(westend?.north?.x ?: 0, 0)
    override val south: Position get() = Position(eastend?.north?.x ?: 0, dimension.height)

}
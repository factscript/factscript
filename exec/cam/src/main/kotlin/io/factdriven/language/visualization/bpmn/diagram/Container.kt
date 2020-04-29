package io.factdriven.language.visualization.bpmn.diagram

class Container(val emptyHeight: Int = 0): Box() {

    constructor(vararg artefact: Artefact): this() {
        artefact.forEach {
            it.westEntryOf(this)
        }
    }

    var westend: Box? = null
    var eastend: Box? = null

    val contained: Set<Box> get() = westend?.connected ?: emptySet()

    override val mostWestern: Artefact? get() = westend?.mostWestern
    override val mostEastern: Artefact? get() = eastend?.mostEastern

    override val allArrows: Set<Arrow> get() = (arrows + contained.map { it.arrows }.flatten()).toSet()

    private lateinit var internalDimension: Dimension

    override val dimension: Dimension get()  {
        if (!::internalDimension.isInitialized) {
            internalDimension = Dimension(
                width = longitudes.map {
                    if (it is Container) (it.contained.map { box -> box.latitudes.map { it.dimension }.sumWidth + box.latitudes.first().greenwich}.max()
                        ?: 0) else it.dimension.width
                }.max()!!,
                // width = longitudes.map { if (it is Container) (it.contained.map { box -> box.latitudes.map { it.dimension }.sumWidth }.max() ?: 0 + box.latitudes.first().greenwich }.max() ?: 0) else it.dimension.width }}.max()!!,
                height = latitudes.map {
                    if (it is Container) (it.contained.map { box -> box.longitudes.map { it.dimension }.sumHeight - box.longitudes.first().west.y + box.longitudes.first().equator }.max()
                        ?: emptyHeight) else it.dimension.height
                }.max()!!)
        }
        return internalDimension
    }

    override val west: Position get() = Position(0, westend?.equator ?: dimension.height / 2)
    override val east: Position get() = Position(dimension.width, eastend?.equator ?: dimension.height / 2)
    override val north: Position get() = Position(westend?.north?.x ?: 0, 0)
    override val south: Position get() = Position(eastend?.north?.x ?: 0, dimension.height)

}
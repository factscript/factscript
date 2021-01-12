package org.factscript.language.visualization.bpmn.diagram

import kotlin.math.max

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

    private var innerDimension: Dimension? = null

    override fun innerDimension(): Dimension {
        innerDimension = innerDimension?: Dimension(
            width = contained.map { box -> (
                    box.latitudes.map { it.dimension }.sumWidth
                            + box.latitudes.first().greenwich
                    )
            }.max() ?: 0,
            height = contained.map { box -> (
                    box.longitudes.map {it.dimension }.sumHeight
                            - box.longitudes.first().west.y
                            + box.longitudes.first().equator
                    )
            }.max() ?: emptyHeight
        )
        return innerDimension!!
    }

    override val west: Position get() = Position(0, westend?.equator ?: latitudeHeight() / 2)
    override val east: Position get() = Position(longitudeWidth(), eastend?.equator ?: latitudeHeight() / 2)
    override val north: Position get() = Position(westend?.north?.x ?: 0, 0)
    override val south: Position get() = Position(eastend?.north?.x ?: 0, latitudeHeight())

}
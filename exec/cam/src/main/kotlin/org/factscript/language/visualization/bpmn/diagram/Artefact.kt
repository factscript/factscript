package org.factscript.language.visualization.bpmn.diagram

open class Artefact(private val inner: Dimension, private val margin: Dimension): Box() {

    constructor(width: Int, height: Int, margin: Int): this(Dimension(width, height), Dimension(margin, margin))

    override fun innerDimension(): Dimension {
        return Dimension(
            inner.width + margin.width * 2, inner.height + margin.height * 2
        )
    }

    val raw: Artefact get() = object: Artefact(inner, margin) {

        override val dimension: Dimension get() = inner
        override val position: Position get() = (this@Artefact.attached?.position ?: this@Artefact.position) + margin

        override val south: Position get() = Position(inner.width / 2, inner.height)
        override val north: Position get() = Position(inner.width /2, 0)
        override val west: Position get() = Position(0, inner.height / 2)
        override val east: Position get() = Position(inner.width, inner.height / 2)

/*
        override val south: Position get() = this@Artefact.south - Dimension(margin.width, margin.height * 2)
        override val north: Position get() = this@Artefact.north - Dimension(margin.width, 0)
        override val west: Position get() = this@Artefact.west - Dimension(0, margin.height)
        override val east: Position get() = this@Artefact.east - Dimension(margin.width * 2, margin.height)
*/

    }

    override val mostWestern: Artefact get() = this
    override val mostEastern: Artefact get() = this

    override val west: Position get() = Position(0, innerDimension().height / 2)
    override val east: Position get() = Position(longitudeWidth(), innerDimension().height / 2)
    override val north: Position get() = Position(longitudeWidth() / 2, 0)
    override val south: Position get() = Position(longitudeWidth() / 2, innerDimension().height)

    override val position: Position get() = super.position.let { if (attached != null) raw.position - margin else it }

}
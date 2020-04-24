package io.factdriven.execution.camunda.diagram

open class Artefact(private val inner: Dimension, private val margin: Int): Box() {

    constructor(width: Int, height: Int, margin: Int): this(Dimension(width, height), margin)

    override val dimension: Dimension = Dimension(
        inner.width + margin * 2, inner.height + margin * 2
    )

    val raw: Artefact get() = object: Artefact(inner, margin) {

        override val dimension: Dimension get() = inner
        override val position: Position get() = this@Artefact.position + Dimension(margin, margin)

        override val south: Position get() = this@Artefact.south - Dimension(margin, margin * 2)
        override val north: Position get() = this@Artefact.north - Dimension(margin, 0)
        override val west: Position get() = this@Artefact.west - Dimension(0, margin)
        override val east: Position get() = this@Artefact.east - Dimension(margin * 2, margin)

    }

    override val westEntry: Artefact get() = this
    override val eastEntry: Artefact get() = this

    override val west: Position get() = Position(0, dimension.height / 2)
    override val east: Position get() = Position(dimension.width, dimension.height / 2)
    override val north: Position get() = Position(dimension.width / 2, 0)
    override val south: Position get() = Position(dimension.width / 2, dimension.height)

}
package io.factdriven.execution.camunda.diagram

open class Artefact(private val innerDimension: Dimension, private val margin: Int): Box() {

    constructor(width: Int, height: Int, margin: Int): this(Dimension(width, height), margin)

    override val dimension: Dimension = Dimension(innerDimension.width + margin * 2, innerDimension.height + margin * 2)

    val inner: Artefact get() = object: Artefact(innerDimension, margin) {
        override val dimension: Dimension get() = innerDimension
        override val position: Position get() = this@Artefact.position + Dimension(margin, margin)
        override val bottomExit: Position get() = this@Artefact.bottomExit - Dimension(0, margin) - Dimension(margin, margin)
        override val topEntry: Position get() = this@Artefact.topEntry + Dimension(0, margin) - Dimension(margin, margin)
        override val leftEntry: Position get() = this@Artefact.leftEntry + Dimension(margin, 0) - Dimension(margin, margin)
        override val rightExit: Position get() = this@Artefact.rightExit - Dimension(margin, 0) - Dimension(margin, margin)
    }

    override val entryArtefact: Artefact get() = this
    override val exitArtefact: Artefact get() = this

    override val leftEntry: Position
        get() = Position(
            0,
            dimension.height / 2
        )
    override val rightExit: Position
        get() = Position(
            dimension.width,
            dimension.height / 2
        )
    override val topEntry: Position
        get() = Position(
            dimension.width / 2,
            0
        )
    override val bottomExit: Position
        get() = Position(
            dimension.width / 2,
            dimension.height
        )

}
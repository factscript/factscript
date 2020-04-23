package io.factdriven.execution.camunda.diagram

abstract class Box: Space {

    var left: Box? = null
    var right: Box? = null
    var onTop: Box? = null
    var below: Box? = null

    abstract val leftEntry: Position
    abstract val rightExit: Position
    abstract val topEntry: Position
    abstract val bottomExit: Position

    var container: Container? = null
    val arrows: MutableList<Arrow> = mutableListOf()

    abstract val entryArtefact: Artefact
    abstract val exitArtefact: Artefact

    override val position: Position get() {
        return when {
            container != null && container!!.entry == this -> container!!.position + container!!.leftEntry - leftEntry
            left != null -> left!!.position + left!!.rightExit - leftEntry
            onTop != null -> onTop!!.position + Dimension(0, onTop!!.dimension.height)
            below != null -> below!!.position - Dimension(0, dimension.height)
            else -> (container?.position ?: Position.Zero) + (container?.leftEntry ?: Position(0, y)) - leftEntry
        }
    }

    fun putRightOf(that: Box) {
        that.putLeftOf(this)
    }

    fun putLeftOf(that: Box) {
        right = that
        that.left = this
        defineAsExit(that)
        associate(that)
    }

    fun putBelowOf(that: Box) {
        that.putOnTopOf(this)
    }

    fun putOnTopOf(that: Box) {
        below = that
        that.onTop = this
        defineAsExit(that)
    }

    fun putInside(that: Container) {
        container = that
        that.entry = this
        that.exit = this
    }

    fun associate(that: Box, via: Position? = null) {
        val arrow = Arrow(this, that, via)
        arrows.add(arrow)
        that.arrows.add(arrow)
    }

    fun associate(that: Box, xIsStickierThanY: Boolean) {
        val arrow = Arrow(this, that, xIsStickierThanY)
        arrows.add(arrow)
        that.arrows.add(arrow)
    }

    private fun defineAsExit(box: Box) {
        container = container ?: box.container
        container?.exit = if (box.container != null) this else box
        box.container = container
    }

    val allLeft: List<Box> get() = (left?.allLeft ?: emptyList()) + listOfNotNull(left)
    val allRight: List<Box> get() = listOfNotNull(right) + (right?.allRight ?: emptyList())
    val allOnTop: List<Box> get() = (onTop?.allOnTop ?: emptyList()) + listOfNotNull(onTop)
    val allBelow: List<Box> get() = listOfNotNull(below) + (below?.allBelow ?: emptyList())

    val allVertical: List<Box> get() = allOnTop + this + allBelow
    val allHorizontal: List<Box> get() = allLeft + this + allRight
    val allSiblings: Set<Box> get() = (allHorizontal.map { it.allVertical + it.allHorizontal } + allVertical.map { it.allVertical + it.allHorizontal }).flatten().toSet()
    open val allArrows: Set<Arrow> get() = arrows.toSet()

    protected val Box.y: Int get() {
        fun Box.y(): Int = leftEntry.y + allOnTop.map { it.dimension }.sumHeight
        return allHorizontal.maxBy { it.y() }!!.y()
    }

}
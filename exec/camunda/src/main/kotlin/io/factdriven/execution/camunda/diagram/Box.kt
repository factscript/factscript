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

    open var container: Container? = null

    override val position: Position get() {
        return if (container?.entry == this) {
            container!!.position + container!!.leftEntry - leftEntry
        } else if (left != null) {
            left!!.position + left!!.rightExit - leftEntry
        } else if (onTop != null) {
            onTop!!.position + Dimension(0, onTop!!.dimension.height)
        } else if (below != null) {
            below!!.position - Dimension(0, dimension.height)
        } else {
            Position.Zero
        }
    }

    fun putRightOf(that: Box) {
        left = that
        that.right = this
        exit(that)
    }

    fun putLeftOf(that: Box) {
        right = that
        that.left = this
        exit(that)
    }

    fun putBelowOf(that: Box) {
        onTop = that
        that.below = this
        exit(that)
    }

    fun putOnTopOf(that: Box) {
        below = that
        that.onTop = this
        exit(that)
    }

    fun putInside(that: Container) {
        container = that
        that.entry = this
        that.exit = this
    }

    private fun exit(box: Box) {
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
    val allSiblings: Set<Box> get() = allHorizontal.map { it.allVertical }.flatten().toSet()

}
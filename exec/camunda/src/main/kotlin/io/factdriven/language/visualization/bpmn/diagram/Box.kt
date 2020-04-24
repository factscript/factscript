package io.factdriven.language.visualization.bpmn.diagram

abstract class Box: Space {

    var western: Box? = null
    var eastern: Box? = null
    var northern: Box? = null
    var southern: Box? = null

    abstract val west: Position
    abstract val east: Position
    abstract val north: Position
    abstract val south: Position

    var container: Container? = null

    val arrows: MutableList<Arrow> = mutableListOf()

    abstract val westEntry: Artefact?
    abstract val eastEntry: Artefact?

    private lateinit var internalPosition: Position

    override val position: Position get() {
        if (!::internalPosition.isInitialized) {
            internalPosition = when {
                container != null && container!!.westend == this -> container!!.position + container!!.west - west
                western != null -> western!!.position + western!!.east - west
                northern != null -> northern!!.position + Dimension(0, northern!!.dimension.height)
                southern != null -> southern!!.position - Dimension(0, dimension.height)
                else -> (container?.position ?: Position.Zero) + (container?.west ?: Position(0, equator)) - west
            }
        }
        return internalPosition
    }

    fun eastOf(that: Box): Box {
        that.westOf(this)
        return that
    }

    fun westOf(that: Box): Box {
        eastern = that
        that.western = this
        eastEntry(that)
        return that
    }

    fun southOf(that: Box): Box {
        that.northOf(this)
        return that
    }

    fun northOf(that: Box): Box {
        southern = that
        that.northern = this
        eastEntry(that)
        return that
    }

    fun insideOf(that: Container): Box {
        container = that
        that.westend = this
        that.eastend = this
        return that
    }

    fun connect(target: Box, via: Box? = null): Box {
        val arrow = Arrow(this, target, via)
        arrows.add(arrow)
        target.arrows.add(arrow)
        return target
    }

    private fun eastEntry(box: Box) {
        container = container ?: box.container
        container?.eastend = if (box.container != null) this else box
        box.container = container
    }

    val allLeft: List<Box> get() = (western?.allLeft ?: emptyList()) + listOfNotNull(western)
    val allRight: List<Box> get() = listOfNotNull(eastern) + (eastern?.allRight ?: emptyList())
    val allOnTop: List<Box> get() = (northern?.allOnTop ?: emptyList()) + listOfNotNull(northern)
    val allBelow: List<Box> get() = listOfNotNull(southern) + (southern?.allBelow ?: emptyList())

    val longitudes: List<Box> get() = allOnTop + this + allBelow
    val latitudes: List<Box> get() = allLeft + this + allRight
    val connected: Set<Box> get() = (latitudes.map { it.longitudes + it.latitudes } + longitudes.map { it.longitudes + it.latitudes }).flatten().toSet()

    open val allArrows: Set<Arrow> get() = arrows.toSet()

    internal val equator: Int get() {
        fun Box.y(): Int = west.y + allOnTop.map { it.dimension }.sumHeight
        return latitudes.maxBy { it.y() }!!.y()
    }

}
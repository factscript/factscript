package io.factdriven.execution.camunda.diagram

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

    abstract val westEntry: Artefact
    abstract val eastEntry: Artefact

    override val position: Position get() {
        return when {
            container != null && container!!.westend == this -> container!!.position + container!!.west - west
            western != null -> western!!.position + western!!.east - west
            northern != null -> northern!!.position + Dimension(0, northern!!.dimension.height)
            southern != null -> southern!!.position - Dimension(0, dimension.height)
            else -> (container?.position ?: Position.Zero) + (container?.west ?: Position(0, equator)) - west
        }
    }

    fun eastOf(that: Box) {
        that.westOf(this)
    }

    fun westOf(that: Box) {
        eastern = that
        that.western = this
        eastEntry(that)
        connect(that)
    }

    fun southOf(that: Box) {
        that.northOf(this)
    }

    fun northOf(that: Box) {
        southern = that
        that.northern = this
        eastEntry(that)
    }

    fun insideOf(that: Container) {
        container = that
        that.westend = this
        that.eastend = this
    }

    fun connect(target: Box, via: Box? = null) {
        val arrow = Arrow(this, target, via)
        arrows.add(arrow)
        target.arrows.add(arrow)
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
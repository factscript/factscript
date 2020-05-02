package io.factdriven.language.visualization.bpmn.diagram

import kotlin.math.max

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

    abstract val mostWestern: Artefact?
    abstract val mostEastern: Artefact?

    internal lateinit var internalPosition: Position

    override val position: Position get() {
        if (!::internalPosition.isInitialized) {
            internalPosition = when {
                container != null && container!!.westend == this -> container!!.position + container!!.west - west
                western != null-> (western?.attached?.original ?: western!!.position) + western!!.east - west
                northern != null -> northern!!.position + Dimension(0, northern!!.dimension.height)
                southern != null -> southern!!.position - Dimension(0, dimension.height)
                else -> (container?.position ?: Position.Zero) + (container?.west ?: Position(0, equator)) - west
            }
        }
        return internalPosition
    }

    private var internalDimension: Dimension? = null

    protected fun longitudeWidth(): Int {
        return longitudes.map { it.innerDimension().width }.max()!!
    }

    protected fun latitudeHeight(): Int {
        return latitudes.map { it.innerDimension().height }.max()!!
    }

    override val dimension: Dimension get() {
        val internalDimension = internalDimension ?: Dimension(
            max(longitudeWidth(), innerDimension().width),
            max(latitudeHeight() + let {
                val mostNorthern = allOnTop.firstOrNull() ?: this
                mostNorthern.west.y - mostNorthern.equator
            }, innerDimension().height)
        )
        return internalDimension
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

    fun westEntryOf(that: Container): Box {
        container = that
        that.westend = this
        that.eastend = this // TODO
        return that
    }

    fun eastEntryOf(that: Container): Box {
        container = that
        that.eastend = this
        return that
    }

    fun connect(target: Box, via: Box? = null): Arrow {
        val arrow = Arrow(this, target, via)
        arrows.add(arrow)
        target.arrows.add(arrow)
        return arrow
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

    internal val greenwich: Int get() {
        fun Box.x(): Int = allLeft.map { it.dimension }.sumWidth
        return longitudes.maxBy { it.x() }!!.x()
    }

    protected var attached: Attached? = null

    fun attachTo(box: Box, vararg direction: Direction = emptyArray()) {
        attached = Attached(this, box, direction.asList())
    }

    abstract fun innerDimension(): Dimension

}
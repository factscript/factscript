package io.factdriven.language.visualization.bpmn.diagram

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class ContainerTest {

    @Test
    fun testChain() {

        val container = Container()

        val box1 = Artefact(2, 2, 1)
        box1.westEntryOf(container)

        val box2 = Artefact(4, 4, 1)
        box2.eastOf(box1)

        val box3 = Artefact(2, 2, 1)
        box3.eastOf(box2)

        assertEquals(setOf(box1, box2, box3), container.contained)
        assertEquals(box1, container.westend)
        assertEquals(box3, container.eastend)

        assertEquals(Dimension(14, 6), container.dimension)

        assertEquals(Position(0, 3), container.west)
        assertEquals(Position(14, 3), container.east)
        assertEquals(Position(2, 0), container.north)
        assertEquals(Position(2, 6), container.south)

        assertEquals(Position.Zero, container.position)
        assertEquals(Position(0, 1), box1.position)
        assertEquals(Position(4, 0), box2.position)
        assertEquals(Position(10, 1), box3.position)

    }

    @Test
    fun testStack() {

        val container = Container()

        val box1 = Artefact(2, 2, 1)
        box1.westEntryOf(container)

        val box2 = Artefact(4, 4, 1)
        box1.northOf(box2)

        val box3 = Artefact(2, 2, 1)
        box3.southOf(box2)

        assertEquals(setOf(box1, box2, box3), container.contained)
        assertEquals(box1, container.westend)
        assertEquals(box3, container.eastend)

        assertEquals(Dimension(6, 14), container.dimension)

        assertEquals(Position(0, 2), container.west)
        assertEquals(Position(6, 12), container.east)
        assertEquals(Position(2, 0), container.north)
        assertEquals(Position(2, 14), container.south)

        assertEquals(Position.Zero, container.position)
        assertEquals(Position(0, 0), box1.position)
        assertEquals(Position(0, 4), box2.position)
        assertEquals(Position(0, 10), box3.position)

    }

    @Test
    fun testMatrix() {

        val container = Container()

        val box1 = Artefact(2, 2, 1)
        val box2 = Artefact(4, 4, 1)
        val box3 = Artefact(2, 2, 1)
        val upper = Artefact(4, 4, 1)
        val lower = Artefact(3, 3, 1)

        box1.westEntryOf(container)
        box2.eastOf(box1)
        box2.southOf(upper)
        box2.northOf(lower)
        box2.westOf(box3)

        assertEquals(setOf(box1, box2, box3, upper, lower), container.contained)
        assertEquals(box1, container.westend)
        assertEquals(box3, container.eastend)

        assertEquals(Dimension(14, 17), container.dimension)

        assertEquals(Position(0, 9), container.west)
        assertEquals(Position(14, 9), container.east)

        assertEquals(Position.Zero, container.position)
        assertEquals(Position(0, 7), box1.position)
        assertEquals(Position(4, 6), box2.position)
        assertEquals(Position(10, 7), box3.position)
        assertEquals(Position(4, 0), upper.position)
        assertEquals(Position(4, 12), lower.position)

    }

}
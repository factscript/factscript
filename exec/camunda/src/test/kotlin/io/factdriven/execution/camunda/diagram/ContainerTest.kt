package io.factdriven.execution.camunda.diagram

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
        box1.putInside(container)

        val box2 = Artefact(4, 4, 1)
        box2.putRightOf(box1)

        val box3 = Artefact(2, 2, 1)
        box3.putRightOf(box2)

        assertEquals(setOf(box1, box2, box3), container.contains)
        assertEquals(box1, container.entry)
        assertEquals(box3, container.exit)

        assertEquals(Dimension(14,6), container.dimension)

        assertEquals(Position(0,3), container.leftEntry)
        assertEquals(Position(14,3), container.rightExit)
        assertEquals(Position(2,0), container.topEntry)
        assertEquals(Position(2,6), container.bottomExit)

        assertEquals(Position.Zero, container.position)
        assertEquals(Position(0,1), box1.position)
        assertEquals(Position(4,0), box2.position)
        assertEquals(Position(10,1), box3.position)

    }

    @Test
    fun testStack() {

        val container = Container()

        val box1 = Artefact(2, 2, 1)
        box1.putInside(container)

        val box2 = Artefact(4, 4, 1)
        box1.putOnTopOf(box2)

        val box3 = Artefact(2, 2, 1)
        box3.putBelowOf(box2)

        assertEquals(setOf(box1, box2, box3), container.contains)
        assertEquals(box1, container.entry)
        assertEquals(box3, container.exit)

        assertEquals(Dimension(6,14), container.dimension)

        assertEquals(Position(0,2), container.leftEntry)
        assertEquals(Position(6,12), container.rightExit)
        assertEquals(Position(2,0), container.topEntry)
        assertEquals(Position(2,14), container.bottomExit)

        assertEquals(Position.Zero, container.position)
        assertEquals(Position(0,0), box1.position)
        assertEquals(Position(0,4), box2.position)
        assertEquals(Position(0,10), box3.position)

    }

    @Test
    fun testMatrix() {

        val container = Container()

        val box1 = Artefact(2, 2, 1)
        val box2 = Artefact(4, 4, 1)
        val box3 = Artefact(2, 2, 1)
        val upper = Artefact(4, 4, 1)
        val lower = Artefact(3, 3, 1)

        box1.putInside(container)
        box2.putRightOf(box1)
        box2.putBelowOf(upper)
        box2.putOnTopOf(lower)
        box2.putLeftOf(box3)

        assertEquals(setOf(box1, box2, box3, upper, lower), container.contains)
        assertEquals(box1, container.entry)
        assertEquals(box3, container.exit)

        assertEquals(Dimension(14,17), container.dimension)

        assertEquals(Position(0,9), container.leftEntry)
        assertEquals(Position(14,9), container.rightExit)

        assertEquals(Position.Zero, container.position)
        assertEquals(Position(0,7), box1.position)
        assertEquals(Position(4,6), box2.position)
        assertEquals(Position(10,7), box3.position)
        assertEquals(Position(4,0), upper.position)
        assertEquals(Position(4,12), lower.position)

    }

}
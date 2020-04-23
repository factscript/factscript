package io.factdriven.execution.camunda.diagram

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class ArtefactTest {

    @Test
    fun testArtefact() {

        val box = Artefact(100, 80, 18)

        assertEquals(Position.Zero, box.position)
        assertEquals(Dimension(136,116), box.dimension)

        assertEquals(Position(0, 58), box.leftEntry)
        assertEquals(Position(136, 58), box.rightExit)
        assertEquals(Position(68, 0), box.topEntry)
        assertEquals(Position(68, 116), box.bottomExit)

        assertEquals(Position(18,18), box.inner.position)
        assertEquals(Dimension(100,80), box.inner.dimension)

        assertEquals(Position(0, 40), box.inner.leftEntry)
        assertEquals(Position(100, 40), box.inner.rightExit)
        assertEquals(Position(50, 0), box.inner.topEntry)
        assertEquals(Position(50, 80), box.inner.bottomExit)

    }

    @Test
    fun testChain() {

        val box1 = Artefact(1, 1, 1)
        val box2 = Artefact(1, 1, 1)
        val box3 = Artefact(1, 1, 1)

        box2.putRightOf(box1)
        box2.putLeftOf(box3)

        assertEquals(listOf(box1, box2, box3), box1.allHorizontal)
        assertEquals(listOf(box1), box1.allVertical)
        assertEquals(emptyList<Box>(), box1.allLeft)
        assertEquals(listOf(box2, box3), box1.allRight)
        assertEquals(emptyList<Box>(), box1.allOnTop)
        assertEquals(emptyList<Box>(), box1.allBelow)

        assertEquals(listOf(box1, box2, box3), box2.allHorizontal)
        assertEquals(listOf(box2), box2.allVertical)
        assertEquals(listOf(box1), box2.allLeft)
        assertEquals(listOf(box3), box2.allRight)
        assertEquals(emptyList<Box>(), box2.allOnTop)
        assertEquals(emptyList<Box>(), box2.allBelow)

        assertEquals(listOf(box1, box2, box3), box3.allHorizontal)
        assertEquals(listOf(box3), box3.allVertical)
        assertEquals(listOf(box1, box2), box3.allLeft)
        assertEquals(emptyList<Box>(), box3.allRight)
        assertEquals(emptyList<Box>(), box3.allOnTop)
        assertEquals(emptyList<Box>(), box3.allBelow)

    }

    @Test
    fun testStack() {

        val box1 = Artefact(1, 1, 1)
        val box2 = Artefact(1, 1, 1)
        val box3 = Artefact(1, 1, 1)

        box2.putBelowOf(box1)
        box2.putOnTopOf(box3)

        assertEquals(listOf(box1, box2, box3), box1.allVertical)
        assertEquals(listOf(box1), box1.allHorizontal)
        assertEquals(listOf(box2, box3), box1.allBelow)
        assertEquals(emptyList<Box>(), box1.allOnTop)
        assertEquals(emptyList<Box>(), box1.allLeft)
        assertEquals(emptyList<Box>(), box1.allRight)

        assertEquals(listOf(box1, box2, box3), box2.allVertical)
        assertEquals(listOf(box2), box2.allHorizontal)
        assertEquals(listOf(box1), box2.allOnTop)
        assertEquals(listOf(box3), box2.allBelow)
        assertEquals(emptyList<Box>(), box2.allLeft)
        assertEquals(emptyList<Box>(), box2.allRight)

        assertEquals(listOf(box1, box2, box3), box3.allVertical)
        assertEquals(listOf(box3), box3.allHorizontal)
        assertEquals(listOf(box1, box2), box3.allOnTop)
        assertEquals(emptyList<Box>(), box3.allBelow)
        assertEquals(emptyList<Box>(), box3.allRight)
        assertEquals(emptyList<Box>(), box3.allLeft)

    }

    @Test
    fun testMatrix() {

        val box1 = Artefact(1, 1, 1)
        val box2 = Artefact(1, 1, 1)
        val box3 = Artefact(1, 1, 1)
        val upper = Artefact(1, 1, 1)
        val lower = Artefact(1, 1, 1)

        box2.putRightOf(box1)
        box2.putLeftOf(box3)
        box2.putBelowOf(upper)
        box2.putOnTopOf(lower)

        assertEquals(listOf<Box>(box1, box2, box3), box1.allHorizontal)
        assertEquals(listOf<Box>(box1), box1.allVertical)
        assertEquals(listOf<Box>(), box1.allLeft)
        assertEquals(listOf<Box>(box2, box3), box1.allRight)
        assertEquals(listOf<Box>(), box1.allOnTop)
        assertEquals(listOf<Box>(), box1.allBelow)

        assertEquals(listOf<Box>(box1, box2, box3), box2.allHorizontal)
        assertEquals(listOf<Box>(upper, box2, lower), box2.allVertical)
        assertEquals(listOf<Box>(box1), box2.allLeft)
        assertEquals(listOf<Box>(box3), box2.allRight)
        assertEquals(listOf<Box>(upper), box2.allOnTop)
        assertEquals(listOf<Box>(lower), box2.allBelow)

        assertEquals(listOf<Box>(box1, box2, box3), box3.allHorizontal)
        assertEquals(listOf<Box>(box3), box3.allVertical)
        assertEquals(listOf<Box>(box1, box2), box3.allLeft)
        assertEquals(listOf<Box>(), box3.allRight)
        assertEquals(listOf<Box>(), box3.allOnTop)
        assertEquals(listOf<Box>(), box3.allBelow)

    }


}
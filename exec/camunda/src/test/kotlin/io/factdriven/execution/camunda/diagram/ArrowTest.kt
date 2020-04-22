package io.factdriven.execution.camunda.diagram

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class ArrowTest {

    @Test
    fun testChain() {

        val box1 = Artefact(1, 1, 1)
        val box2 = Artefact(1, 1, 1)
        val box3 = Artefact(1, 1, 1)

        box2.putRightOf(box1)
        box2.putLeftOf(box3)

        assertEquals(Arrow(box1, box2), box1.arrows[0])
        assertEquals(Arrow(box1, box2), box2.arrows[0])
        assertEquals(Arrow(box2, box3), box2.arrows[1])
        assertEquals(Arrow(box2, box3), box3.arrows[0])

    }

    @Test
    fun testStack() {

        val box1 = Artefact(1, 1, 1)
        val box2 = Artefact(1, 1, 1)
        val box3 = Artefact(1, 1, 1)

        box2.putBelowOf(box1)
        box2.putOnTopOf(box3)

        assertEquals(0, box1.arrows.size)
        assertEquals(0, box2.arrows.size)
        assertEquals(0, box3.arrows.size)

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

        box1.associate(upper)
        box1.associate(lower)
        upper.associate(box3)
        lower.associate(box3)

        assertEquals(3, box1.arrows.size)
        assertEquals(Arrow(box1, box2), box1.arrows[0])
        assertEquals(Arrow(box1, upper), box1.arrows[1])
        assertEquals(Arrow(box1, lower), box1.arrows[2])

        assertEquals(2, box2.arrows.size)
        assertEquals(Arrow(box1, box2), box2.arrows[0])
        assertEquals(Arrow(box2, box3), box2.arrows[1])

        assertEquals(3, box3.arrows.size)
        assertEquals(Arrow(box2, box3), box3.arrows[0])
        assertEquals(Arrow(upper, box3), box3.arrows[1])
        assertEquals(Arrow(lower, box3), box3.arrows[2])

        assertEquals(2, upper.arrows.size)
        assertEquals(Arrow(box1, upper), upper.arrows[0])
        assertEquals(Arrow(upper, box3), upper.arrows[1])

        assertEquals(2, lower.arrows.size)
        assertEquals(Arrow(box1, lower), lower.arrows[0])
        assertEquals(Arrow(lower, box3), lower.arrows[1])

    }


}
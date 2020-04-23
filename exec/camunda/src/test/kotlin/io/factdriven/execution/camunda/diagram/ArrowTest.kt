package io.factdriven.execution.camunda.diagram

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class ArrowTest {

    @Test
    fun testChain() {

        val box1 = Artefact(50, 50, 10)
        val box2 = Artefact(100, 100, 10)
        val box3 = Artefact(50, 50, 10)

        box2.putRightOf(box1)
        box2.putLeftOf(box3)

        val arrow1 = box1.arrows[0]
        val arrow2 = box2.arrows[1]

        assertEquals(Arrow(box1, box2), arrow1)
        assertEquals(Arrow(box2, box3), arrow2)

        assertEquals(arrow1, box1.arrows[0])
        assertEquals(arrow1, box2.arrows[0])
        assertEquals(arrow2, box2.arrows[1])
        assertEquals(arrow2, box3.arrows[0])

        assertEquals(Position(10,35), box1.inner.position)
        assertEquals(Position(80,10), box2.inner.position)
        assertEquals(Position(200,35), box3.inner.position)

        assertEquals(Position(0,25), box1.inner.leftEntry)
        assertEquals(Position(50,25), box1.inner.rightExit)

        assertEquals(Position(0,50), box2.inner.leftEntry)
        assertEquals(Position(100,50), box2.inner.rightExit)

        assertEquals(Position(0,25), box3.inner.leftEntry)
        assertEquals(Position(50,25), box3.inner.rightExit)

        assertEquals(listOf(Position(60,60), Position(80,60)), arrow1.wayPoints())
        assertEquals(listOf(Position(180,60), Position(200,60)), arrow2.wayPoints())

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

        val box1 = Artefact(2, 2, 1)
        val box2 = Artefact(2, 2, 1)
        val box3 = Artefact(2, 2, 1)
        val upper = Artefact(2, 2, 1)
        val lower = Artefact(2, 2, 1)

        box2.putRightOf(box1)
        box2.putLeftOf(box3)
        box2.putBelowOf(upper)
        box2.putOnTopOf(lower)

        box1.associate(upper)
        box1.associate(lower)
        upper.associate(box3, false)
        lower.associate(box3, false)

        assertEquals(3, box1.arrows.size)
        assertEquals(Arrow(box1, box2), box1.arrows[0])
        assertEquals(Arrow(box1, upper), box1.arrows[1])
        assertEquals(Arrow(box1, lower), box1.arrows[2])

        assertEquals(2, box2.arrows.size)
        assertEquals(Arrow(box1, box2), box2.arrows[0])
        assertEquals(Arrow(box2, box3), box2.arrows[1])

        assertEquals(3, box3.arrows.size)
        assertEquals(Arrow(box2, box3), box3.arrows[0])
        assertEquals(Arrow(upper, box3, false), box3.arrows[1])
        assertEquals(Arrow(lower, box3, false), box3.arrows[2])

        assertEquals(2, upper.arrows.size)
        assertEquals(Arrow(box1, upper), upper.arrows[0])
        assertEquals(Arrow(upper, box3, false), upper.arrows[1])

        assertEquals(2, lower.arrows.size)
        assertEquals(Arrow(box1, lower), lower.arrows[0])
        assertEquals(Arrow(lower, box3, false), lower.arrows[1])

        val arrow1 = box1.arrows[1]
        val arrow2 = box1.arrows[0]
        val arrow3 = box1.arrows[2]
        val arrow4 = box3.arrows[1]
        val arrow5 = box3.arrows[0]
        val arrow6 = box3.arrows[2]

        assertEquals(listOf(Position(2,5), Position(2,2), Position(5,2)), arrow1.wayPoints())
        assertEquals(listOf(Position(3,6), Position(5,6)), arrow2.wayPoints())
        assertEquals(listOf(Position(2,7), Position(2,10), Position(5,10)), arrow3.wayPoints())
        assertEquals(listOf(Position(7,2), Position(10,2), Position(10,5)), arrow4.wayPoints())
        assertEquals(listOf(Position(7,6), Position(9,6)), arrow5.wayPoints())
        assertEquals(listOf(Position(7,10), Position(10,10), Position(10,7)), arrow6.wayPoints())

    }


}
package io.factdriven.language.visualization.bpmn.diagram

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

        box2.eastOf(box1)
        box2.westOf(box3)

        box1.connect(box2)
        box2.connect(box3)

        val arrow1 = box1.arrows[0]
        val arrow2 = box2.arrows[1]

        assertEquals(Arrow(box1, box2), arrow1)
        assertEquals(Arrow(box2, box3), arrow2)

        assertEquals(arrow1, box1.arrows[0])
        assertEquals(arrow1, box2.arrows[0])
        assertEquals(arrow2, box2.arrows[1])
        assertEquals(arrow2, box3.arrows[0])

        assertEquals(Position(10, 35), box1.raw.position)
        assertEquals(Position(80, 10), box2.raw.position)
        assertEquals(Position(200, 35), box3.raw.position)

        assertEquals(Position(0, 25), box1.raw.west)
        assertEquals(Position(50, 25), box1.raw.east)

        assertEquals(Position(0, 50), box2.raw.west)
        assertEquals(Position(100, 50), box2.raw.east)

        assertEquals(Position(0, 25), box3.raw.west)
        assertEquals(Position(50, 25), box3.raw.east)

        assertEquals(listOf(
            Position(60, 60),
            Position(80, 60)
        ), arrow1.waypoints)
        assertEquals(listOf(
            Position(180, 60),
            Position(200, 60)
        ), arrow2.waypoints)

    }

    @Test
    fun testStack() {

        val box1 = Artefact(1, 1, 1)
        val box2 = Artefact(1, 1, 1)
        val box3 = Artefact(1, 1, 1)

        box2.southOf(box1)
        box2.northOf(box3)

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

        box2.eastOf(box1)
        box2.westOf(box3)
        box2.southOf(upper)
        box2.northOf(lower)

        box1.connect(box2)
        box2.connect(box3)
        box1.connect(upper)
        box1.connect(lower)
        upper.connect(box3)
        lower.connect(box3)

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

        val arrow1 = box1.arrows[1]
        val arrow2 = box1.arrows[0]
        val arrow3 = box1.arrows[2]
        val arrow4 = box3.arrows[1]
        val arrow5 = box3.arrows[0]
        val arrow6 = box3.arrows[2]

        assertEquals(listOf(
            Position(2, 5),
            Position(2, 2),
            Position(5, 2)
        ), arrow1.waypoints)
        assertEquals(listOf(
            Position(3, 6),
            Position(5, 6)
        ), arrow2.waypoints)
        assertEquals(listOf(
            Position(2, 7),
            Position(2, 10),
            Position(5, 10)
        ), arrow3.waypoints)
        assertEquals(listOf(
            Position(7, 2),
            Position(10, 2),
            Position(10, 5)
        ), arrow4.waypoints)
        assertEquals(listOf(
            Position(7, 6),
            Position(9, 6)
        ), arrow5.waypoints)
        assertEquals(listOf(
            Position(7, 10),
            Position(10, 10),
            Position(10, 7)
        ), arrow6.waypoints)

    }

    @Test
    fun testContainer() {

        val process = Container()
        val box1 = Artefact(2, 2, 1)
        val branch = Container()
        val fork = Artefact(6, 6, 1)
        val task = Container(
            Artefact(
                4,
                4,
                1
            )
        )
        val upper = Container(
            Artefact(
                8,
                4,
                1
            )
        )
        val lower = Container(
            Artefact(
                4,
                4,
                1
            )
        )
        val lower2 = Container(
            Artefact(
                4,
                4,
                1
            )
        )
        val join = Artefact(6, 6, 1)
        val box3 = Artefact(2, 2, 1)

        box1.westEntryOf(process)
        branch.eastOf(box1)
        box3.eastOf(branch)

        fork.westEntryOf(branch)
        task.eastOf(fork)
        upper.northOf(task)
        lower.southOf(task)
        lower2.southOf(lower)
        join.eastOf(task)

        assertEquals(Position(0, 7), box1.position)
        assertEquals(Dimension(4, 19), box1.dimension)

        assertEquals(6, branch.contained.size)

        assertEquals(Position(4, 0), branch.position)
        assertEquals(Dimension(26, 26), branch.dimension)

        assertEquals(Position(4, 5), fork.position)
        assertEquals(Dimension(8, 8), fork.dimension)

        assertEquals(Position(12, 0), upper.position)
        assertEquals(Dimension(10, 6), upper.dimension)

        assertEquals(Position(12, 6), task.position)
        assertEquals(Dimension(10, 8), task.dimension)

        assertEquals(Position(12, 14), lower.position)
        assertEquals(Dimension(10, 6), lower.dimension)

        assertEquals(Position(12, 20), lower2.position)
        assertEquals(Dimension(10, 6), lower2.dimension)

        assertEquals(Position(22, 5), join.position)
        assertEquals(Dimension(8, 8), join.dimension)

        assertEquals(Position(30, 7), box3.position)
        assertEquals(Dimension(4, 19), box3.dimension)

        assertEquals(Dimension(34, 26), process.dimension)

    }

}
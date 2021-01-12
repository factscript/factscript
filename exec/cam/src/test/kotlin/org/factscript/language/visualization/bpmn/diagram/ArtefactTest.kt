package org.factscript.language.visualization.bpmn.diagram

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
        assertEquals(Dimension(136, 116), box.dimension)

        assertEquals(Position(0, 58), box.west)
        assertEquals(Position(136, 58), box.east)
        assertEquals(Position(68, 0), box.north)
        assertEquals(Position(68, 116), box.south)

        assertEquals(Position(18, 18), box.raw.position)
        assertEquals(Dimension(100, 80), box.raw.dimension)

        assertEquals(Position(0, 40), box.raw.west)
        assertEquals(Position(100, 40), box.raw.east)
        assertEquals(Position(50, 0), box.raw.north)
        assertEquals(Position(50, 80), box.raw.south)

    }

    @Test
    fun testChain() {

        val box1 = Artefact(1, 1, 1)
        val box2 = Artefact(1, 1, 1)
        val box3 = Artefact(1, 1, 1)

        box2.eastOf(box1)
        box2.westOf(box3)

        assertEquals(listOf(box1, box2, box3), box1.latitudes)
        assertEquals(listOf(box1), box1.longitudes)
        assertEquals(emptyList<Box>(), box1.allLeft)
        assertEquals(listOf(box2, box3), box1.allRight)
        assertEquals(emptyList<Box>(), box1.allOnTop)
        assertEquals(emptyList<Box>(), box1.allBelow)

        assertEquals(listOf(box1, box2, box3), box2.latitudes)
        assertEquals(listOf(box2), box2.longitudes)
        assertEquals(listOf(box1), box2.allLeft)
        assertEquals(listOf(box3), box2.allRight)
        assertEquals(emptyList<Box>(), box2.allOnTop)
        assertEquals(emptyList<Box>(), box2.allBelow)

        assertEquals(listOf(box1, box2, box3), box3.latitudes)
        assertEquals(listOf(box3), box3.longitudes)
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

        box2.southOf(box1)
        box2.northOf(box3)

        assertEquals(listOf(box1, box2, box3), box1.longitudes)
        assertEquals(listOf(box1), box1.latitudes)
        assertEquals(listOf(box2, box3), box1.allBelow)
        assertEquals(emptyList<Box>(), box1.allOnTop)
        assertEquals(emptyList<Box>(), box1.allLeft)
        assertEquals(emptyList<Box>(), box1.allRight)

        assertEquals(listOf(box1, box2, box3), box2.longitudes)
        assertEquals(listOf(box2), box2.latitudes)
        assertEquals(listOf(box1), box2.allOnTop)
        assertEquals(listOf(box3), box2.allBelow)
        assertEquals(emptyList<Box>(), box2.allLeft)
        assertEquals(emptyList<Box>(), box2.allRight)

        assertEquals(listOf(box1, box2, box3), box3.longitudes)
        assertEquals(listOf(box3), box3.latitudes)
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

        box2.eastOf(box1)
        box2.westOf(box3)
        box2.southOf(upper)
        box2.northOf(lower)

        assertEquals(listOf<Box>(box1, box2, box3), box1.latitudes)
        assertEquals(listOf<Box>(box1), box1.longitudes)
        assertEquals(listOf<Box>(), box1.allLeft)
        assertEquals(listOf<Box>(box2, box3), box1.allRight)
        assertEquals(listOf<Box>(), box1.allOnTop)
        assertEquals(listOf<Box>(), box1.allBelow)

        assertEquals(listOf<Box>(box1, box2, box3), box2.latitudes)
        assertEquals(listOf<Box>(upper, box2, lower), box2.longitudes)
        assertEquals(listOf<Box>(box1), box2.allLeft)
        assertEquals(listOf<Box>(box3), box2.allRight)
        assertEquals(listOf<Box>(upper), box2.allOnTop)
        assertEquals(listOf<Box>(lower), box2.allBelow)

        assertEquals(listOf<Box>(box1, box2, box3), box3.latitudes)
        assertEquals(listOf<Box>(box3), box3.longitudes)
        assertEquals(listOf<Box>(box1, box2), box3.allLeft)
        assertEquals(listOf<Box>(), box3.allRight)
        assertEquals(listOf<Box>(), box3.allOnTop)
        assertEquals(listOf<Box>(), box3.allBelow)

    }


}
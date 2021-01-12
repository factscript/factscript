package org.factscript.language.execution.cam.compensation

import org.factscript.execution.*
import org.factscript.language.Flows.activate
import org.factscript.language.execution.cam.*
import org.factscript.language.visualization.bpmn.model.*
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class ShipmentTest: TestHelper() {

    private val kClass = Shipment::class
    private lateinit var command: Any
    private lateinit var id: String

    init { activate(kClass).forEach { BpmnModel(it).toTempFile(true) }  }

    @Test
    fun testShipment() {

        command = ShipGoods("myOrderId")
        id = send(kClass, command)

        val instance = kClass.load(id)

        assertEquals("myOrderId", instance.orderId)
        assertEquals(true, instance.shipped)

    }

}
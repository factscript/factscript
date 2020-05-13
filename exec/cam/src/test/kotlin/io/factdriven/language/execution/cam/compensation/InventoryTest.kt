package io.factdriven.language.execution.cam.compensation

import io.factdriven.execution.*
import io.factdriven.language.Flows.activate
import io.factdriven.language.execution.cam.*
import io.factdriven.language.visualization.bpmn.model.*
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class InventoryTest: TestHelper() {

    private val kClass = Inventory::class
    private lateinit var command: Any
    private lateinit var id: String

    init { activate(kClass).forEach { BpmnModel(it).toTempFile(true) }  }

    @Test
    fun testInventory() {

        command = FetchGoodsFromInventory("myOrderId")
        id = send(kClass, command)

        var instance = kClass.load(id)

        assertEquals("myOrderId", instance.orderId)
        assertEquals(true, instance.fetched)

        command = ReturnGoodsToInventory("myOrderId")
        send(kClass, command)

        instance = kClass.load(id)

        assertEquals(false, instance.fetched)

    }

}
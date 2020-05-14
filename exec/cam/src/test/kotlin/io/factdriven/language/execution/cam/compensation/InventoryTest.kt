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

    private lateinit var command: Any
    private lateinit var id: String

    init { activate(Inventory1::class, Inventory2::class).forEach { BpmnModel(it).toTempFile(true) }  }

    @Test
    fun testInventory() {

        command = FetchGoodsFromInventory("myOrderId")
        id = send(Inventory1::class, command)

        val instance1 = Inventory1::class.load(id)

        assertEquals("myOrderId", instance1.orderId)
        assertEquals(true, instance1.fetched)

        command = ReturnGoodsToInventory("myOrderId")
        id = send(Inventory2::class, command)

        val instance2 = Inventory2::class.load(id)

        assertEquals(true, instance2.returned)

    }

}
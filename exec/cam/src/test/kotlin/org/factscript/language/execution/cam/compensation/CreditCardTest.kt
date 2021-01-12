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
class CreditCardTest: TestHelper() {

    private val kClass = CreditCard::class
    private lateinit var command: Any
    private lateinit var id: String

    init { activate(kClass).forEach { script -> BpmnModel(script).toTempFile(true) }  }

    @Test
    fun testSuccess() {

        val reference = "paymentId"
        val charge = 5F

        command = ChargeCreditCard(reference, charge)
        id = send(kClass, command)

        var instance = kClass.load(id)

        assertEquals(reference, instance.reference)
        assertEquals(charge, instance.charge)
        assertEquals(false, instance.confirmed)
        assertEquals(false, instance.successful)

        command = ConfirmationReceived(reference, true)
        send(kClass, command)

        instance = kClass.load(id)

        assertEquals(true, instance.confirmed)
        assertEquals(true, instance.successful)

    }

    @Test
    fun testFailure() {

        val reference = "paymentId"
        val charge = 5F

        command = ChargeCreditCard(reference, charge)
        id = send(kClass, command)

        var instance = kClass.load(id)

        assertEquals(reference, instance.reference)
        assertEquals(charge, instance.charge)
        assertEquals(false, instance.confirmed)
        assertEquals(false, instance.successful)

        command = ConfirmationReceived(reference, false)
        send(kClass, command)

        instance = kClass.load(id)

        assertEquals(false, instance.confirmed)
        assertEquals(false, instance.successful)

    }

}
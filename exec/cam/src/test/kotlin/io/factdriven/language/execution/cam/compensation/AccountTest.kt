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
class AccountTest: TestHelper() {

    private val kClass = Account::class
    private lateinit var command: Any
    private lateinit var id: String

    init { activate(kClass).forEach { script -> BpmnModel(script).toTempFile(true) }  }

    @Test
    fun testWithdraw5() {

        command = WithdrawAmountFromCustomerAccount(customer = "kermit", withdraw = 5F)
        id = send(kClass, command)

        var instance = kClass.load(id)

        assertEquals(0F, instance.balance)

        command = CreditAmountToCustomerAccount(customer = "kermit", credit = 5F)
        send(kClass, command)

        instance = kClass.load(id)

        assertEquals(5F, instance.balance)

    }

    @Test
    fun testWithdraw10() {

        command = WithdrawAmountFromCustomerAccount(customer = "kermit", withdraw = 10F)
        id = send(kClass, command)

        var instance = kClass.load(id)

        assertEquals(0F, instance.balance)

        command = CreditAmountToCustomerAccount(customer = "kermit", credit = 10F)
        send(kClass, command)

        instance = kClass.load(id)

        assertEquals(10F, instance.balance)

    }

}
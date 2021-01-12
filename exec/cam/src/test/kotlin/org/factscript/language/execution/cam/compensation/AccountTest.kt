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
class AccountTest: TestHelper() {

    private lateinit var command: Any
    private lateinit var id: String

    init { activate(Account1::class, Account2::class).forEach { script -> BpmnModel(script).toTempFile(true) }  }

    @Test
    fun testWithdraw5() {

        command = WithdrawAmountFromCustomerAccount(customer = "kermit", withdraw = 5F)
        id = send(Account1::class, command)

        val instance1 = Account1::class.load(id)

        assertEquals(0F, instance1.balance)

        command = CreditAmountToCustomerAccount(customer = "kermit", credit = 5F)
        id = send(Account2::class, command)

        val instance2 = Account2::class.load(id)

        assertEquals(5F, instance2.balance)

    }

    @Test
    fun testWithdraw10() {

        command = WithdrawAmountFromCustomerAccount(customer = "kermit", withdraw = 10F)
        id = send(Account1::class, command)

        val instance1 = Account1::class.load(id)

        assertEquals(0F, instance1.balance)

        command = CreditAmountToCustomerAccount(customer = "kermit", credit = 10F)
        id = send(Account2::class, command)

        val instance2 = Account2::class.load(id)

        assertEquals(10F, instance2.balance)

    }

}
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
class PaymentTest: TestHelper() {

    private val kClass = arrayOf(Payment::class, Account::class, CreditCard::class)
    private lateinit var fact: Any
    private lateinit var id: String

    init { activate(*kClass).forEach { script -> BpmnModel(script).toTempFile(true) }  }

    @Test
    fun testPaymentCoveredbyAccountBalance() {

        val orderId = "orderId"
        val accountId = "kermit"
        val charge = 5F

        fact = RetrievePayment(orderId, accountId, charge)
        id = send(kClass[0], fact)

        val instance = Payment::class.load(id)

        assertEquals(orderId, instance.orderId)
        assertEquals(accountId, instance.accountId)
        assertEquals(charge, instance.total)

        assertEquals(5F, instance.covered)
        assertEquals(true, instance.successful)
        assertEquals(false, instance.failed)

    }

    @Test
    fun testPaymentCoveredByValidCreditCard() {

        val orderId = "orderId"
        val accountId = "kermit"
        val charge = 10F

        fact = RetrievePayment(orderId, accountId, charge)
        id = send(Payment::class, fact)

        var instance = Payment::class.load(id)

        assertEquals(orderId, instance.orderId)
        assertEquals(accountId, instance.accountId)
        assertEquals(charge, instance.total)

        assertEquals(5F, instance.covered)
        assertEquals(false, instance.successful)
        assertEquals(false, instance.failed)

        fact = ConfirmationReceived(orderId, true)
        send(CreditCard::class, fact)

        instance = Payment::class.load(id)

        assertEquals(10F, instance.covered)
        assertEquals(true, instance.successful)
        assertEquals(false, instance.failed)

    }

    @Test
    fun testPaymentCoveredAfterUpdatingExpiredCreditCard() {

        val orderId = "orderId"
        val accountId = "kermit"
        val charge = 10F

        fact = RetrievePayment(orderId, accountId, charge)
        id = send(Payment::class, fact)

        var instance = Payment::class.load(id)

        assertEquals(orderId, instance.orderId)
        assertEquals(accountId, instance.accountId)
        assertEquals(charge, instance.total)

        assertEquals(5F, instance.covered)
        assertEquals(false, instance.successful)
        assertEquals(false, instance.failed)
        assertEquals(false, instance.expired)

        fact = ConfirmationReceived(orderId, false)
        send(CreditCard::class, fact)

        instance = Payment::class.load(id)

        assertEquals(5F, instance.covered)
        assertEquals(false, instance.successful)
        assertEquals(false, instance.failed)
        assertEquals(true, instance.expired)

        fact = CreditCardDetailsUpdated(accountId)
        send(Payment::class, fact)

        instance = Payment::class.load(id)

        assertEquals(5F, instance.covered)
        assertEquals(false, instance.successful)
        assertEquals(false, instance.failed)

        fact = ConfirmationReceived(orderId, true)
        send(CreditCard::class, fact)

        instance = Payment::class.load(id)

        assertEquals(10F, instance.covered)
        assertEquals(true, instance.successful)
        assertEquals(false, instance.failed)

    }

    @Test
    fun testPaymentFailed() {

        val orderId = "orderId"
        val accountId = "kermit"
        val charge = 10F

        fact = RetrievePayment(orderId, accountId, charge)
        id = send(Payment::class, fact)

        var instance = Payment::class.load(id)

        assertEquals(orderId, instance.orderId)
        assertEquals(accountId, instance.accountId)
        assertEquals(charge, instance.total)

        assertEquals(5F, instance.covered)
        assertEquals(false, instance.successful)
        assertEquals(false, instance.failed)
        assertEquals(false, instance.expired)

        fact = ConfirmationReceived(orderId, false)
        send(CreditCard::class, fact)

        instance = Payment::class.load(id)

        assertEquals(5F, instance.covered)
        assertEquals(false, instance.successful)
        assertEquals(false, instance.failed)
        assertEquals(true, instance.expired)

        sleep(60, 5000)

        instance = Payment::class.load(id)

        assertEquals(0F, instance.covered)
        assertEquals(false, instance.successful)
        assertEquals(true, instance.failed)

    }

}